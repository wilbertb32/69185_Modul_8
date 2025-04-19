package ac.id.umn.if570_modul_8

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

class StudentViewModel : ViewModel() {
    private val db = Firebase.firestore
    var students by mutableStateOf(listOf<Student>())
        private set

    init {
        fetchStudents()
    }

    fun addStudent (student: Student) {
        val studentMap = hashMapOf(
            "id" to student.id,
            "name" to student.name,
            "program" to student.program
        )

        db.collection("students")
            .add(studentMap)
            .addOnSuccessListener { docRef ->
                Log.d("FireStore", "Student added with ID: ${docRef.id}")
                // Tambahkan setiap nomor ke subcollection "phones"
                student.phones.forEach { phone ->
                    val phoneMap = hashMapOf("number" to phone)
                    docRef.collection("phones").add(phoneMap)
                }
                fetchStudents() // Refresh Data
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding student", e)
            }
    }

    private fun fetchStudents() {
        db.collection("students")
            .get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<Student>()
                for (document in result) {
                    val id = document.getString("id") ?: ""
                    val name = document.getString("name") ?: ""
                    val program = document.getString("program") ?: ""
                    val docId = document.id

                    // Ambil subcollection phones
                    db.collection("students").document(docId).collection("phones")
                        .get()
                        .addOnSuccessListener { phoneResult ->
                            val phones = phoneResult.mapNotNull { it.getString("number") }
                            val student = Student(id, name, program, phones)
                            list.add(student)
                            students = list
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error getting phones", e)
                        }
                }

            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting students.", exception)
            }
    }
}