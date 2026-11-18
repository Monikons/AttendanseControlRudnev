package com.example.attendancecontrol

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale

class AttendanceActivity : AppCompatActivity() {

    private lateinit var spinnerCreateGroup: Spinner
    private lateinit var spinnerLessons: Spinner
    private lateinit var etLessonDate: EditText
    private lateinit var etSubject: EditText
    private lateinit var tvHint: TextView
    private lateinit var attendanceList: LinearLayout
    private lateinit var btnSave: Button

    private val pendingStatuses = mutableMapOf<Pair<Int, Int>, String>()
    private var selectedLesson: Lesson? = null
    private var rendering = false

    private val statusOptions = listOf(
        "Присутствовал",
        "Отсутствовал",
        "Опоздал"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        spinnerCreateGroup = findViewById(R.id.spinnerCreateGroup)
        spinnerLessons = findViewById(R.id.spinnerLessons)
        etLessonDate = findViewById(R.id.etLessonDate)
        etSubject = findViewById(R.id.etSubject)
        tvHint = findViewById(R.id.tvAttendanceHint)
        attendanceList = findViewById(R.id.layoutAttendanceList)
        btnSave = findViewById(R.id.btnSaveAttendance)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnCreateLesson).setOnClickListener { createLesson() }
        btnSave.setOnClickListener { saveAttendance() }

        refreshGroups()
        refreshLessons()

        spinnerLessons.onItemSelectedListener = SimpleItemSelectedListener { position ->
            if (!rendering) {
                selectedLesson = AttendanceData.lessons.getOrNull(position)
                renderStudents()
            }
        }
    }

    private fun refreshGroups() {
        val groups = AttendanceData.students.map { it.groupName }.distinct().sorted()
        val values = if (groups.isEmpty()) listOf("Нет доступных групп") else groups
        spinnerCreateGroup.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            values
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun refreshLessons() {
        val lessons = AttendanceData.lessons
        val values = if (lessons.isEmpty()) {
            listOf("Занятий пока нет")
        } else {
            lessons.map { "${it.groupName} • ${it.lessonDate} • ${it.subject}" }
        }

        rendering = true
        spinnerLessons.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            values
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        rendering = false

        if (lessons.isNotEmpty()) {
            selectedLesson = lessons.last()
            spinnerLessons.setSelection(lessons.lastIndex)
            renderStudents()
        } else {
            selectedLesson = null
            attendanceList.removeAllViews()
            tvHint.text = "Сначала создайте занятие."
            btnSave.isEnabled = false
        }
    }

    private fun createLesson() {
        val groupPosition = spinnerCreateGroup.selectedItemPosition
        val groups = AttendanceData.students.map { it.groupName }.distinct().sorted()
        val group = groups.getOrNull(groupPosition)
        val date = etLessonDate.text.toString().trim()
        val subject = etSubject.text.toString().trim()

        if (group == null) {
            Toast.makeText(this, "Сначала добавьте хотя бы одного студента", Toast.LENGTH_LONG).show()
            return
        }
        if (!isValidDate(date)) {
            etLessonDate.error = "Введите корректную дату ДД.ММ.ГГГГ"
            return
        }
        if (subject.isEmpty()) {
            etSubject.error = "Укажите дисциплину или тему"
            return
        }

        val existing = AttendanceData.lessons.firstOrNull {
            it.groupName == group && it.lessonDate == date && it.subject.equals(subject, true)
        }
        if (existing != null) {
            Toast.makeText(this, "Такое занятие уже существует", Toast.LENGTH_SHORT).show()
            selectedLesson = existing
            refreshLessons()
            spinnerLessons.setSelection(AttendanceData.lessons.indexOf(existing))
            return
        }

        val lesson = AttendanceData.addLesson(group, date, subject)
        pendingStatuses.clear()
        refreshLessons()
        spinnerLessons.setSelection(AttendanceData.lessons.indexOf(lesson))
        etLessonDate.text.clear()
        etSubject.text.clear()
        Toast.makeText(this, "Занятие создано", Toast.LENGTH_SHORT).show()
    }

    private fun isValidDate(value: String): Boolean {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
        format.isLenient = false
        val position = ParsePosition(0)
        val date = format.parse(value, position)
        return date != null && position.index == value.length
    }

    private fun renderStudents() {
        attendanceList.removeAllViews()
        val lesson = selectedLesson
        if (lesson == null) {
            tvHint.text = "Выберите занятие."
            btnSave.isEnabled = false
            return
        }

        val students = AttendanceData.students
            .filter { it.groupName == lesson.groupName }
            .sortedWith(compareBy<Student> { it.lastName }.thenBy { it.firstName })

        if (students.isEmpty()) {
            tvHint.text = "В группе «${lesson.groupName}» пока нет студентов."
            btnSave.isEnabled = false
            return
        }

        tvHint.text = "Группа: ${lesson.groupName}. Выберите один статус для каждого студента."
        btnSave.isEnabled = true

        students.forEach { student ->
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 12, 0, 12)
            }

            val name = TextView(this).apply {
                text = "${student.lastName} ${student.firstName}"
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val radioGroup = RadioGroup(this).apply {
                orientation = RadioGroup.VERTICAL
            }

            val savedStatus = pendingStatuses[lesson.id to student.id]
                ?: AttendanceData.getAttendance(lesson, student.id)?.status

            statusOptions.forEach { status ->
                val radioButton = RadioButton(this).apply {
                    text = status
                    tag = status
                    id = View.generateViewId()
                    isChecked = savedStatus == status
                }
                radioGroup.addView(radioButton)
            }

            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                if (checkedId != -1) {
                    val selected = group.findViewById<RadioButton>(checkedId)
                    pendingStatuses[lesson.id to student.id] = selected.tag.toString()
                }
            }

            container.addView(name)
            container.addView(radioGroup)
            attendanceList.addView(container)

            val separator = View(this).apply {
                setBackgroundColor(0xFFE0E0E0.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                )
            }
            attendanceList.addView(separator)
        }
    }

    private fun saveAttendance() {
        val lesson = selectedLesson
        if (lesson == null) {
            Toast.makeText(this, "Сначала выберите занятие", Toast.LENGTH_SHORT).show()
            return
        }

        val students = AttendanceData.students.filter { it.groupName == lesson.groupName }
        if (students.isEmpty()) {
            Toast.makeText(this, "В выбранной группе нет студентов", Toast.LENGTH_SHORT).show()
            return
        }

        val missing = students.filter { student ->
            (pendingStatuses[lesson.id to student.id]
                ?: AttendanceData.getAttendance(lesson, student.id)?.status) == null
        }

        if (missing.isNotEmpty()) {
            Toast.makeText(
                this,
                "Выберите статус для каждого студента. Не отмечено: ${missing.size}",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        students.forEach { student ->
            val status = pendingStatuses[lesson.id to student.id]
                ?: AttendanceData.getAttendance(lesson, student.id)?.status
            if (status != null) {
                AttendanceData.saveAttendance(lesson, student.id, status)
            }
        }

        pendingStatuses.clear()
        renderStudents()
        Toast.makeText(this, "Отметки сохранены", Toast.LENGTH_SHORT).show()
    }
}
