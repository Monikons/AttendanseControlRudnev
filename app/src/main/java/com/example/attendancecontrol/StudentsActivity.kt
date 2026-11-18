package com.example.attendancecontrol

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StudentsActivity : AppCompatActivity() {

    private val studentList get() = AttendanceData.students
    private lateinit var adapter: StudentAdapter
    private var nextId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_students)

        nextId = (studentList.maxOfOrNull { it.id } ?: 0) + 1

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val btnAddStudent: Button = findViewById(R.id.btnAddStudent)
        val btnBack: Button = findViewById(R.id.btnBack)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StudentAdapter(studentList) { student -> showDeleteDialog(student) }
        recyclerView.adapter = adapter

        btnAddStudent.setOnClickListener { showAddStudentDialog() }
        btnBack.setOnClickListener { finish() }
        updateEmptyState()
    }

    private fun updateEmptyState() {
        val emptyView = findViewById<TextView?>(R.id.tvEmptyStudents)
        emptyView?.visibility = if (studentList.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun showAddStudentDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_student, null)
        val etLastName = dialogView.findViewById<EditText>(R.id.etLastName)
        val etFirstName = dialogView.findViewById<EditText>(R.id.etFirstName)
        val etGroup = dialogView.findViewById<EditText>(R.id.etGroup)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Новый студент")
            .setView(dialogView)
            .setPositiveButton("Сохранить", null)
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val lastName = etLastName.text.toString().trim()
                val firstName = etFirstName.text.toString().trim()
                val group = etGroup.text.toString().trim()

                if (lastName.isEmpty() || firstName.isEmpty() || group.isEmpty()) {
                    Toast.makeText(this, "Заполните фамилию, имя и группу", Toast.LENGTH_SHORT).show()
                } else {
                    studentList.add(Student(nextId++, firstName, lastName, group))
                    adapter.notifyItemInserted(studentList.size - 1)
                    updateEmptyState()
                    Toast.makeText(this, "Студент добавлен", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun showDeleteDialog(student: Student) {
        AlertDialog.Builder(this)
            .setTitle("Удаление")
            .setMessage("Удалить ${student.lastName} ${student.firstName}?")
            .setPositiveButton("Да") { _, _ ->
                val index = studentList.indexOf(student)
                if (index != -1) {
                    studentList.removeAt(index)
                    adapter.notifyItemRemoved(index)
                    updateEmptyState()
                }
            }
            .setNegativeButton("Нет", null)
            .show()
    }
}
