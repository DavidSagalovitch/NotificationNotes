package com.example.notificationnotes.data

import android.util.Log
import com.example.notificationnotes.globalappContext
import com.example.notificationnotes.isUserSignedIn
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

fun isUserSignedin(): Boolean{
	// Check if the user is signed in (non-null) and update UI accordingly.
	val currentUser = FirebaseAuth.getInstance().currentUser
	if (currentUser != null) {
		return false
	} else {
		return true
	}
}

fun signOut(){
	AuthUI.getInstance()
		.signOut(globalappContext)
		.addOnCompleteListener {
			if (it.isSuccessful) {
				isUserSignedIn.value = false
			} else {
				// Handle sign-out failure
			}
		}
}

fun checkAndSetUserData(userId: String, email: String) {
	val databaseReference = FirebaseDatabase.getInstance().getReference("users")
	val userReference = databaseReference.child(userId)

	userReference.addListenerForSingleValueEvent(object : ValueEventListener {
		override fun onDataChange(snapshot: DataSnapshot) {
			if (!snapshot.exists()) {
				// User data does not exist, set the data
				val initialNotes = mapOf(
					"note1" to mapOf(
						"title" to " ",
						"content" to " ",
						"noteID" to "2"
					)
				)
				val userData = mapOf(
					"userId" to userId,
					"email" to email,
					"notes" to initialNotes
					// Add any other default data you want for the new user
				)
				userReference.setValue(userData)
					.addOnSuccessListener {
						Log.d("DATABASE", "User data set for $email")
					}
					.addOnFailureListener { e ->
						Log.w("DATABASE", "Setting user data failed", e)
					}
			} else {
				Log.d("DATABASE", "User data already exists for $email")
			}
		}

		override fun onCancelled(error: DatabaseError) {
			Log.w("DATABASE", "Failed to read value.", error.toException())
		}
	})
}

/**
 * Checks if a user exists by email in Firebase Realtime Database.
 *
 * @param email The email address to check.
 * @param callback A callback function that will be called with the result (true if exists, false otherwise).
 */
fun doesUserExist(email: String, callback: (Boolean) -> Unit) {
	val databaseReference = FirebaseDatabase.getInstance().getReference("users")

	databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
		ValueEventListener {
		override fun onDataChange(snapshot: DataSnapshot) {
			// If the snapshot exists and has children, it means the email was found
			val exists = snapshot.exists() && snapshot.children.iterator().hasNext()
			callback(exists)
		}

		override fun onCancelled(error: DatabaseError) {
			// Handle possible errors
			Log.e("doesUserExist", "Database error: $error")
			callback(false)
		}
	})
}

fun findNotesByEmail(userEmail: String, callback: (Map<String, Map<String, String>>?) -> Unit) {
	val databaseReference = FirebaseDatabase.getInstance().getReference("users")

	databaseReference.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(object :
		ValueEventListener {
		override fun onDataChange(snapshot: DataSnapshot) {
			if (snapshot.exists() && snapshot.children.count() > 0) {
				val userSnapshot = snapshot.children.first()

				val notes: Map<String, Map<String, String>>? = userSnapshot.child("notes").getValue(object : GenericTypeIndicator<Map<String, Map<String, String>>>() {})

				callback(notes)
			} else {
				callback(null)
			}
		}

		override fun onCancelled(error: DatabaseError) {
			Log.w("findNotesByEmail", "Database error: $error")
			callback(null)
		}
	})
}

fun addOrUpdateNoteByEmail(email: String, noteId: String, note: Map<String, String>) {
	val databaseReference = FirebaseDatabase.getInstance().getReference("users")

	databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
		ValueEventListener {
		override fun onDataChange(snapshot: DataSnapshot) {
			if (snapshot.exists()) {
				for (userSnapshot in snapshot.children) {
					val notesReference = userSnapshot.child("notes").ref

					val noteData = mapOf(
						noteId to note
					)

					notesReference.updateChildren(noteData as Map<String, Any>)
				}
			}
		}

		override fun onCancelled(error: DatabaseError) {
			Log.w("addOrUpdateNoteByEmail", "Database error: $error")
		}
	})
}
fun removeNoteByEmail(email: String, noteId: String) {
	val databaseReference = FirebaseDatabase.getInstance().getReference("users")

	databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
		ValueEventListener {
		override fun onDataChange(snapshot: DataSnapshot) {
			if (snapshot.exists()) {
				for (userSnapshot in snapshot.children) {
					if (userSnapshot.child("notes").exists()) {
						// Directly remove the note by noteId
						userSnapshot.child("notes").child(noteId).ref.removeValue()
					}
				}
			}
		}

		override fun onCancelled(error: DatabaseError) {
			Log.w("removeNoteByEmail", "Database error: $error")
		}
	})
}

fun extractIntegerFromNoteID(noteID: String): Int {
	val regex = "\\d+".toRegex()
	val matchResult = regex.find(noteID)
	return matchResult?.value?.toIntOrNull() ?: 0 // Default to 0 if no integer is found
}