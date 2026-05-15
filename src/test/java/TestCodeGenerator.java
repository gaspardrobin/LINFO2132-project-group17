import static org.junit.Assert.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import org.junit.Test;

import compiler.Compiler;

public class TestCodeGenerator {

    private String compileAndRun(String source, String input) throws Exception {
        Path tempDir = Files.createTempDirectory("codegen-test");
        Path sourceFile = tempDir.resolve("test.lang");
        Path classFile = tempDir.resolve("Generated.class");

        Files.writeString(sourceFile, source, StandardCharsets.UTF_8);

        Compiler.main(new String[] {
            sourceFile.toString(),
            "-o",
            classFile.toString()
        });

        Process process = new ProcessBuilder("java", "-cp", tempDir.toString(), "Generated")
                .redirectErrorStream(true)
                .start();

        if (input != null && !input.isEmpty()) {
            try (OutputStream os = process.getOutputStream()) {
                os.write(input.getBytes(StandardCharsets.UTF_8));
            }
        }

        String output;
        try (InputStream is = process.getInputStream()) {
            output = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        int exitCode = process.waitFor();

        assertEquals(
            "Generated program should exit with code 0. Output:\n" + output,
            0,
            exitCode
        );
        return output.replace("\r\n", "\n");
    }

    @Test
    public void testPrintingAndBasicOperations() throws Exception {
        String source = """
            def main() {
                print_INT(1);
                println("");
                print_FLOAT(1.1);
                println("");
                print("hello");
                println("");

                INT a = 10;
                INT b = 5;

                print_INT(a + b);
                println("");
                print_INT(a - b);
                println("");
                print_INT(-a);
                println("");
                print_INT(a * b);
                println("");
                print_INT(a / b);
                println("");
                print_INT(a % b);
                println("");

                FLOAT fa = 10.5;
                FLOAT fb = 5.2;

                print_FLOAT(fa + fb);
                println("");
                print_FLOAT(fa - fb);
                println("");
                print_FLOAT(-fa);
                println("");
                print_FLOAT(fa * fb);
                println("");
            }
            """;

        String output = compileAndRun(source, "");

        assertEquals("""
            1
            1.1
            hello
            15
            5
            -10
            50
            2
            0
            15.7
            5.3
            -10.5
            54.6
            """, output);
    }

    @Test
    public void testGlobalsAndConstants() throws Exception {
        String source = """
            final INT i = 3;
            final FLOAT j = 3.2 * 5.0;
            final INT k = i * 3;
            final STRING message = "Hello";
            final BOOL ok = true;

            INT a = 3;
            INT[] arr = INT ARRAY [2];

            def main() {
                print_INT(i);
                println("");
                print_FLOAT(j);
                println("");
                print_INT(k);
                println("");
                print(message);
                println("");

                if (ok) {
                    print("true");
                } else {
                    print("false");
                }
                println("");

                print_INT(a);
                println("");

                arr[0] = 10;
                arr[1] = 20;

                print_INT(arr[0]);
                println("");
                print_INT(arr[1]);
                println("");
            }
            """;

        String output = compileAndRun(source, "");

        assertEquals("""
            3
            16.0
            9
            Hello
            true
            3
            10
            20
            """, output);
    }

    @Test
    public void testCollectionsAndFieldAccess() throws Exception {
        String source = """
            coll Point {
                INT x;
                INT y;
            }

            coll Person {
                STRING name;
                Point location;
                INT[] history;
            }

            def main() {
                Point p = Point(3, 7);

                print_INT(p.x);
                println("");
                print_INT(p.y);
                println("");

                Person person = Person("me", Point(1, 2), INT ARRAY [4]);

                print(person.name);
                println("");
                print_INT(person.location.x);
                println("");

                person.location.x = 99;

                print_INT(person.location.x);
                println("");
            }
            """;

        String output = compileAndRun(source, "");

        assertEquals("""
            3
            7
            me
            1
            99
            """, output);
    }

    @Test
    public void testArraysOfCollectionsAndReturnCollection() throws Exception {
        String source = """
            coll Point {
                INT x;
                INT y;
            }

            def Point copyPoints(Point[] p) {
                return Point(p[0].x + p[1].x, p[0].y + p[1].y);
            }

            def main() {
                Point[] points = Point ARRAY [2];

                points[0] = Point(1, 2);
                points[1] = Point(3, 4);

                Point combined = copyPoints(points);

                print_INT(combined.x);
                println("");
                print_INT(combined.y);
                println("");
            }
            """;

        String output = compileAndRun(source, "");

        assertEquals("""
            4
            6
            """, output);
    }

    @Test
    public void testLoopsAndConditions() throws Exception {
        String source = """
            def main() {
                INT i = 0;
                INT result = 0;

                while (i < 5) {
                    i = i + 1;
                    result = result + i;
                }

                print_INT(result);
                println("");

                for (i; 1 -> 5; i + 1) {
                    if (i > 3) {
                        print("High ");
                    } else {
                        print("Low ");
                    }
                }
                println("");
            }
            """;

        String output = compileAndRun(source, "");

        assertEquals("""
            15
            Low Low Low High
            """, output.stripTrailing() + "\n");
    }

    @Test
    public void testReadInt() throws Exception {
        String source = """
            def main() {
                INT value = read_INT();
                print_INT(value);
                println("");
            }
            """;

        String output = compileAndRun(source, "2\n");

        assertEquals("""
            2
            """, output);
    }

    @Test
    public void testReadFloat() throws Exception {
        String source = """
            def main() {
                FLOAT f = read_FLOAT();

                print_INT(floor(f));
                println("");
                print_INT(ceil(f));
                println("");
            }
            """;

        String output = compileAndRun(source, "3.7\n");

        assertEquals("""
            3
            4
            """, output);
    }

    @Test
    public void testReadStringAndOtherNatives() throws Exception {
        String source = """
            def main() {
                STRING s = read_STRING();
                print(s);
                println("");

                print(str(65));
                println("");

                print_INT(length("hello"));
                println("");

                INT[] arr = INT ARRAY [3];
                print_INT(length(arr));
                println("");
            }
            """;

        String output = compileAndRun(source, "hello\n");

        assertEquals("""
            hello
            A
            5
            3
            """, output);
    }

    @Test
    public void testIntToFloat() throws Exception {
        String source = """
            final FLOAT globalFloat = 3;

            def main() {
                FLOAT localFloat = 4;
                print_FLOAT(globalFloat);
                println("");
                print_FLOAT(localFloat);
                println("");
            }
            """;

        String output = compileAndRun(source, "");

        assertEquals("""
            3.0
            4.0
            """, output);
    }
    @Test
    public void testPrintlnWithoutArgument() throws Exception {
        String source = """
            def main() {
                print("Hello");
                println();
                print("World");
                println();
            }
            """;

        String output = compileAndRun(source, "");

        assertEquals("""
            Hello
            World
            """, output);
    }
}