import java.io.IOException;

class PortalException extends Exception {
    PortalException(String message) { super(message); }
    PortalException(String message, Throwable cause) { super(message, cause); }
}
class ExamWindowClosedException extends PortalException {
    ExamWindowClosedException(String m) { super(m); }
}
class StudentNotRegisteredException extends PortalException {
    StudentNotRegisteredException(String m) { super(m); }
}
class SubmissionStorageException2 extends PortalException {
    SubmissionStorageException2(String m, Throwable c) { super(m, c); }
}
class PortalStorage {
    void store(String fileName) throws SubmissionStorageException2 {
        try {
            if (fileName.endsWith(".bad")) throw new IOException("Simulated storage I/O failure");
            System.out.println("Stored " + fileName);
        } catch (IOException e) {
            throw new SubmissionStorageException2("Unable to save submission", e);
        }
    }
}
class ExamService {
    private final PortalStorage storage = new PortalStorage();
    void submit(boolean windowOpen, boolean registered, String file)
            throws PortalException {
        if (!windowOpen) throw new ExamWindowClosedException("Exam window has closed");
        if (!registered) throw new StudentNotRegisteredException("Student is not registered");
        storage.store(file);
    }
}
public class PortalDemo {
    public static void main(String[] args) {
        ExamService service = new ExamService();
        boolean[][] tests = {{true, true}, {false, true}, {true, false}};
        String[] files = {"answer.pdf", "answer.pdf", "answer.pdf"};
        for (int i = 0; i < tests.length; i++) {
            try {
                service.submit(tests[i][0], tests[i][1], files[i]);
            } catch (PortalException e) {
                System.out.println("Student message: " + e.getMessage());
                if (e.getCause() != null)
                    System.out.println("Original cause: " + e.getCause().getMessage());
            }
        }
        try {
            service.submit(true, true, "broken.bad");
        } catch (PortalException e) {
            System.out.println("Student message: " + e.getMessage());
            System.out.println("Cause: " + e.getCause().getMessage());
            e.printStackTrace(System.out);
        }
    }
}
