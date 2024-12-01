package ems;
import java.util.*;

// Author: 752381@pdsb.net
public class MyHashTable {

	// ATTRIBUTES

	// buckets is an array of ArrayList.  Each item in the array holds the
	// reference value of an ArrayList.  Each item in an ArrayList holds
	// a reference value pointing to a student.

	public ArrayList<EmployeeInfo>[] buckets;
	private int length = 0;
	public int length() { return length; }


	// CONSTRUCTOR

	@SuppressWarnings("unchecked")
	public MyHashTable(int howManyBuckets) {
		// Construct the hash table (open hashing/closed addressing) as an array of howManyBuckets ArrayLists.

		// Instantiate buckets as an array to have an ArrayList as each element of the array.
		buckets = new ArrayList[howManyBuckets];

		// For each element in the array, instantiate its ArrayList.
		for (int i = 0; i < howManyBuckets; i++) {
			buckets[i] = new ArrayList<>();  // Instantiate the ArrayList for bucket i.
		}
	}


	// METHODS

	public int calcBucket(int sN) {
		return(sN % buckets.length);  // student number modulo number of buckets
	}


	public void addToTable(EmployeeInfo employee) {
		// Add the student referenced by employee to the hash table.

		if (employee == null) {
			return; // Nothing to do!
		}

		buckets[calcBucket(employee.empNum)].add(employee);
		length++;

	}  // end addToTable


	public EmployeeInfo removeFromTable(int empNum) {
		// Remove that student from the hash table and return the reference value for that student.
		// Return null if that student isn't in the table.

		// CODE GOES HERE
		EmployeeInfo r = null;
		ListIterator<EmployeeInfo> it = buckets[calcBucket(empNum)].listIterator();
		while (it.hasNext()) {
			EmployeeInfo e = it.next();
			if (e.empNum == empNum) {
				r = e;
				it.remove();
				length--;
				break;
			}
		}

		return r;

	}  // end removeFromTable


	public EmployeeInfo getFromTable(int empNum) {
		return buckets[calcBucket(empNum)].stream().filter(e -> e.empNum == empNum).findAny().orElse(null);
	}  // end getFromTable


	public void displayTable() {

		// Walk through the buckets and display the items in each bucket's ArrayList.

		System.out.println("\n\nHERE ARE THE CONTENTS OF THE TABLE:\n");
		for (int i = 0; i < buckets.length; i++) {
			if (buckets[i].isEmpty()) {
				System.out.printf("BUCKET %d has an empty ArrayList!\n", i);
			}
			else {
				System.out.printf("BUCKET %d has an ArrayList holding the following students:\n", i);
				// Print out the first name of each student in the ArrayList for the current bucket.
				for (EmployeeInfo e : buckets[i]) {
					System.out.printf("\t%d\t%s %s\n", e.empNum, e.firstName, e.lastName);
				}
			}
		}

	} // end displayTable


}
