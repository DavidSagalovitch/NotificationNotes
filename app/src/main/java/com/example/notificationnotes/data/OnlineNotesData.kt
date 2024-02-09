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
				val initialNotes = mapOf("noteId2" to " ")
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

fun findNotesByEmail(userEmail: String, callback: (Map<String, String>?) -> Unit) {
	val databaseReference = FirebaseDatabase.getInstance().getReference("users")

	// Query the database for users with the specified email
	databaseReference.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(object :
		ValueEventListener {
		override fun onDataChange(snapshot: DataSnapshot) {
			if (snapshot.exists() && snapshot.children.count() > 0) {
				// Assuming each email is unique, so there should only be one matching user
				val userSnapshot = snapshot.children.first()

				// Retrieve the notes data for this user
				val notes: Map<String, String>? = userSnapshot.child("notes").getValue(object : GenericTypeIndicator<Map<String, String>>() {})

				// Use the callback to return the notes
				callback(notes)
			} else {
				// User not found or no notes exist, return null
				callback(null)
			}
		}

		override fun onCancelled(error: DatabaseError) {
			// Handle possible errors
			Log.w("findNotesByEmail", "Database error: $error")
			callback(null)
		}
	})
}

fun addOrUpdateNoteByEmail(email: String, noteId: String, note: String) {
	val databaseReference = FirebaseDatabase.getInstance().getReference("users")

	databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
		ValueEventListener {
		override fun onDataChange(snapshot: DataSnapshot) {
			if (snapshot.exists()) {
				for (userSnapshot in snapshot.children) {
					// Check if the user has a notes field
					val notesReference = userSnapshot.child("notes").ref

					val noteData = mapOf(
						noteId to note
					)

					notesReference.updateChildren(noteData)
				}
			}
		}

		override fun onCancelled(error: DatabaseError) {
			// Handle possible errors
			Log.w("addNoteByEmailAndContent", "Database error: $error")
		}
	})
}

fun removeNoteByEmail(email: String, note: String) {
	val databaseReference = FirebaseDatabase.getInstance().getReference("users")

	databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
		ValueEventListener {
		override fun onDataChange(snapshot: DataSnapshot) {
			if (snapshot.exists()) {
				for (userSnapshot in snapshot.children) {
					// Check if the user has a notes field
					if (userSnapshot.child("notes").exists()) {
						val notesMap = userSnapshot.child("notes").value as? Map<String, String>

						if (notesMap != null) {
							// Iterate through the notes and find the one with matching content
							val noteIdToRemove = notesMap.entries.firstOrNull { it.value == note }?.key

							if (noteIdToRemove != null) {
								// Remove the note by noteId
								userSnapshot.child("notes").child(noteIdToRemove).ref.removeValue()
							}
						}
					}
				}
			}
		}

		override fun onCancelled(error: DatabaseError) {
			// Handle possible errors
			Log.w("removeNoteByEmailAndContent", "Database error: $error")
		}
	})
}

fun extractIntegerFromNoteID(noteID: String): Int {
	val regex = "\\d+".toRegex()
	val matchResult = regex.find(noteID)
	return matchResult?.value?.toIntOrNull() ?: 0 // Default to 0 if no integer is found
}