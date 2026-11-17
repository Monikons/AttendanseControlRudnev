package com.example.attendancecontrol

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StudentsActivity : AppCompatActivity() {

    // Временный список (в Спринте 4 заменим на базу данных Room)
    private val studentList = mutableListOf<Student>()
    private lateinit var adapter: StudentAdapter
    private var nextId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_students)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val btnAddStudent: Button = findViewById(R.id.btnAddStudent)
        val btnBack: Button = findViewById(R.id.btnBack)

        // Настраиваем список
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StudentAdapter(studentList) { student -> showDeleteDialog(student) }
        recyclerView.adapter = adapter

        // Кнопка "Добавить"
        btnAddStudent.setOnClickListener {
            showAddStudentDialog()
        }

        // Кнопка "Назад"
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showAddStudentDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_student, null)
        val etLastName = dialogView.findViewById<EditText>(R.id.etLastName)
        val etFirstName = dialogView.findViewById<EditText>(R.id.etFirstName)
        val etGroup = dialogView.findViewById<EditText>(R.id.etGroup)

        AlertDialog.Builder(this)
            .setTitle("Новый студент")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val lastName = etLastName.text.toString().trim()
                val firstName = etFirstName.text.toString().trim()
                val group = etGroup.text.toString().trim()

                // Проверка на пустые поля
                if (lastName.isEmpty() || firstName.isEmpty() || group.isEmpty()) {
                    Toast.makeText(this, "Ошибка: Заполните все поля!", Toast.LENGTH_SHORT).show()
                } else {
                    val newStudent = Student(nextId++, firstName, lastName, group)
                    studentList.add(newStudent)
                    adapter.notifyItemInserted(studentList.size - 1)
                    Toast.makeText(this, "Студент добавлен", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
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
                }
            }
            .setNegativeButton("Нет", null)
            .show()
    }
}