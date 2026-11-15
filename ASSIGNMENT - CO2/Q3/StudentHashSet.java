import java.util.HashSet;
import java.util.Objects;

class Student {
    int id;
    String name;

    Student(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Student)) return false;

        Student other = (Student) obj;
        return id == other.id && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}

public class StudentHashSet {
    public static void main(String[] args) {

        HashSet<Student> students = new HashSet<>();

        Student s1 = new Student(101, "Arthi");
        Student s2 = new Student(102, "Priya");
        Student s3 = new Student(101, "Arthi");

        students.add(s1);
        students.add(s2);
        students.add(s3);

        System.out.println("Students in HashSet:");
        for (Student s : students) {
            System.out.println(s);
        }

        System.out.println("Total students: " + students.size());
        System.out.println("s1 equals s3: " + s1.equals(s3));
        System.out.println("s1 hashCode: " + s1.hashCode());
        System.out.println("s3 hashCode: " + s3.hashCode());
    }
}
