package com.preppilot.config;

import com.preppilot.entity.CodingQuestion;
import com.preppilot.repository.CodingQuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeds the coding_questions table with starter problems on first launch.
 * Only inserts when the table is empty, so subsequent restarts are a no-op.
 */
@Component
@Order(1)
public class CodingQuestionSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CodingQuestionSeeder.class);

    private final CodingQuestionRepository repo;

    public CodingQuestionSeeder(CodingQuestionRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) {
            log.info("Coding questions already seeded ({} found), skipping.", repo.count());
            return;
        }

        log.info("Seeding coding questions...");

        // ── 1. Two Sum ──────────────────────────────────────────────
        save("Two Sum", "EASY", "Arrays",
            "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.\n\nRead input as a line of space-separated integers, then the target on the next line. Print the two indices space-separated.\n\nExample:\nInput:\n2 7 11 15\n9\nOutput:\n0 1",
            boilerplate(
                "nums = list(map(int, input().split()))\ntarget = int(input())\n# your code here",
                "import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    String[] parts = sc.nextLine().trim().split(\" \");\n    int[] nums = new int[parts.length];\n    for (int i = 0; i < parts.length; i++) nums[i] = Integer.parseInt(parts[i]);\n    int target = Integer.parseInt(sc.nextLine().trim());\n    // your code here\n  }\n}",
                "const lines = require(\"fs\").readFileSync(0, \"utf8\").split(\"\\n\");\nconst nums = lines[0].trim().split(\" \").map(Number);\nconst target = parseInt(lines[1]);\n// your code here"
            ),
            testCases(
                tc("2 7 11 15\n9", "0 1", false),
                tc("3 2 4\n6", "1 2", false),
                tc("3 3\n6", "0 1", true)
            )
        );

        // ── 2. Reverse String ───────────────────────────────────────
        save("Reverse String", "EASY", "Strings",
            "Given a string, reverse it and print the result.\n\nRead the string from standard input.\n\nExample:\nInput: hello\nOutput: olleh",
            boilerplate(
                "s = input()\n# your code here",
                "import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    String s = new Scanner(System.in).nextLine();\n    // your code here\n  }\n}",
                "const s = require(\"fs\").readFileSync(0, \"utf8\").trim();\n// your code here"
            ),
            testCases(
                tc("hello", "olleh", false),
                tc("OpenAI", "IAnepO", false),
                tc("racecar", "racecar", true),
                tc("a", "a", true)
            )
        );

        // ── 3. FizzBuzz ─────────────────────────────────────────────
        save("FizzBuzz", "EASY", "Logic",
            "Print numbers from 1 to n. For multiples of 3 print \"Fizz\", for multiples of 5 print \"Buzz\", for multiples of both print \"FizzBuzz\".\n\nRead n from standard input. Print one value per line.\n\nExample:\nInput: 5\nOutput:\n1\n2\nFizz\n4\nBuzz",
            boilerplate(
                "n = int(input())\n# your code here",
                "import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    int n = Integer.parseInt(new Scanner(System.in).nextLine().trim());\n    // your code here\n  }\n}",
                "const n = parseInt(require(\"fs\").readFileSync(0, \"utf8\").trim());\n// your code here"
            ),
            testCases(
                tc("5", "1\n2\nFizz\n4\nBuzz", false),
                tc("15", "1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\nBuzz\n11\nFizz\n13\n14\nFizzBuzz", true)
            )
        );

        // ── 4. Valid Parentheses ─────────────────────────────────────
        save("Valid Parentheses", "MEDIUM", "Stacks",
            "Given a string containing just the characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid.\n\nPrint \"true\" or \"false\".\n\nA string is valid if:\n- Open brackets are closed by the same type of brackets.\n- Open brackets are closed in the correct order.\n\nExample:\nInput: ()[]{}\nOutput: true",
            boilerplate(
                "s = input().strip()\n# your code here",
                "import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    String s = new Scanner(System.in).nextLine().trim();\n    // your code here\n  }\n}",
                "const s = require(\"fs\").readFileSync(0, \"utf8\").trim();\n// your code here"
            ),
            testCases(
                tc("()", "true", false),
                tc("()[]{}", "true", false),
                tc("(]", "false", false),
                tc("([)]", "false", true),
                tc("{[]}", "true", true)
            )
        );

        // ── 5. Palindrome Check ─────────────────────────────────────
        save("Palindrome Check", "EASY", "Strings",
            "Given a string, determine if it is a palindrome, considering only alphanumeric characters and ignoring cases.\n\nPrint \"true\" or \"false\".\n\nExample:\nInput: A man, a plan, a canal: Panama\nOutput: true",
            boilerplate(
                "s = input()\n# your code here",
                "import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    String s = new Scanner(System.in).nextLine();\n    // your code here\n  }\n}",
                "const s = require(\"fs\").readFileSync(0, \"utf8\").trim();\n// your code here"
            ),
            testCases(
                tc("A man, a plan, a canal: Panama", "true", false),
                tc("race a car", "false", false),
                tc(" ", "true", true),
                tc("ab", "false", true)
            )
        );

        // ── 6. Maximum Subarray (Kadane's) ──────────────────────────
        save("Maximum Subarray", "MEDIUM", "Arrays",
            "Given an integer array nums, find the subarray with the largest sum, and return its sum.\n\nRead the array as space-separated integers from stdin.\n\nExample:\nInput: -2 1 -3 4 -1 2 1 -5 4\nOutput: 6\n\nExplanation: The subarray [4,-1,2,1] has the largest sum 6.",
            boilerplate(
                "nums = list(map(int, input().split()))\n# your code here",
                "import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    String[] parts = sc.nextLine().trim().split(\" \");\n    int[] nums = new int[parts.length];\n    for (int i = 0; i < parts.length; i++) nums[i] = Integer.parseInt(parts[i]);\n    // your code here\n  }\n}",
                "const nums = require(\"fs\").readFileSync(0, \"utf8\").trim().split(\" \").map(Number);\n// your code here"
            ),
            testCases(
                tc("-2 1 -3 4 -1 2 1 -5 4", "6", false),
                tc("1", "1", false),
                tc("5 4 -1 7 8", "23", true),
                tc("-1", "-1", true)
            )
        );

        // ── 7. Merge Two Sorted Arrays ──────────────────────────────
        save("Merge Two Sorted Arrays", "EASY", "Arrays",
            "You are given two sorted integer arrays. Merge them into one sorted array and print the result as space-separated integers.\n\nInput: First line is array 1, second line is array 2.\n\nExample:\nInput:\n1 3 5\n2 4 6\nOutput:\n1 2 3 4 5 6",
            boilerplate(
                "a = list(map(int, input().split()))\nb = list(map(int, input().split()))\n# your code here",
                "import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    String[] p1 = sc.nextLine().trim().split(\" \");\n    String[] p2 = sc.nextLine().trim().split(\" \");\n    int[] a = Arrays.stream(p1).mapToInt(Integer::parseInt).toArray();\n    int[] b = Arrays.stream(p2).mapToInt(Integer::parseInt).toArray();\n    // your code here\n  }\n}",
                "const lines = require(\"fs\").readFileSync(0, \"utf8\").split(\"\\n\");\nconst a = lines[0].trim().split(\" \").map(Number);\nconst b = lines[1].trim().split(\" \").map(Number);\n// your code here"
            ),
            testCases(
                tc("1 3 5\n2 4 6", "1 2 3 4 5 6", false),
                tc("1 2 3\n4 5 6", "1 2 3 4 5 6", false),
                tc("1\n2", "1 2", true)
            )
        );

        // ── 8. Climbing Stairs ──────────────────────────────────────
        save("Climbing Stairs", "MEDIUM", "Dynamic Programming",
            "You are climbing a staircase. It takes n steps to reach the top. Each time you can either climb 1 or 2 steps. In how many distinct ways can you climb to the top?\n\nRead n from stdin, print the number of ways.\n\nExample:\nInput: 3\nOutput: 3\n\nExplanation: 1+1+1, 1+2, 2+1.",
            boilerplate(
                "n = int(input())\n# your code here",
                "import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    int n = Integer.parseInt(new Scanner(System.in).nextLine().trim());\n    // your code here\n  }\n}",
                "const n = parseInt(require(\"fs\").readFileSync(0, \"utf8\").trim());\n// your code here"
            ),
            testCases(
                tc("2", "2", false),
                tc("3", "3", false),
                tc("5", "8", true),
                tc("10", "89", true)
            )
        );

        // ── 9. Longest Common Prefix ────────────────────────────────
        save("Longest Common Prefix", "EASY", "Strings",
            "Write a function to find the longest common prefix string amongst an array of strings.\n\nIf there is no common prefix, print an empty line.\n\nInput: strings separated by spaces on a single line.\n\nExample:\nInput: flower flow flight\nOutput: fl",
            boilerplate(
                "words = input().split()\n# your code here",
                "import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    String[] words = new Scanner(System.in).nextLine().trim().split(\" \");\n    // your code here\n  }\n}",
                "const words = require(\"fs\").readFileSync(0, \"utf8\").trim().split(\" \");\n// your code here"
            ),
            testCases(
                tc("flower flow flight", "fl", false),
                tc("dog racecar car", "", false),
                tc("interspecies interstellar interstate", "inters", true),
                tc("a", "a", true)
            )
        );

        // ── 10. Container With Most Water ───────────────────────────
        save("Container With Most Water", "HARD", "Arrays",
            "You are given an integer array height of length n. There are n vertical lines drawn such that the two endpoints of the ith line are (i, 0) and (i, height[i]).\n\nFind two lines that together with the x-axis form a container, such that the container contains the most water. Return the maximum amount of water a container can store.\n\nRead the array as space-separated integers. Print the maximum area.\n\nExample:\nInput: 1 8 6 2 5 4 8 3 7\nOutput: 49",
            boilerplate(
                "heights = list(map(int, input().split()))\n# your code here",
                "import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    String[] parts = sc.nextLine().trim().split(\" \");\n    int[] height = new int[parts.length];\n    for (int i = 0; i < parts.length; i++) height[i] = Integer.parseInt(parts[i]);\n    // your code here\n  }\n}",
                "const height = require(\"fs\").readFileSync(0, \"utf8\").trim().split(\" \").map(Number);\n// your code here"
            ),
            testCases(
                tc("1 8 6 2 5 4 8 3 7", "49", false),
                tc("1 1", "1", false),
                tc("4 3 2 1 4", "16", true),
                tc("1 2 1", "2", true)
            )
        );

        log.info("Seeded {} coding questions.", repo.count());
    }

    // ── helpers ──────────────────────────────────────────────────────

    private void save(String title, String difficulty, String category,
                      String description, String boilerplate, String testCases) {
        CodingQuestion q = new CodingQuestion();
        q.setTitle(title);
        q.setDifficulty(difficulty);
        q.setCategory(category);
        q.setDescription(description);
        q.setBoilerplate(boilerplate);
        q.setTestCases(testCases);
        repo.save(q);
    }

    private static String boilerplate(String python, String java, String javascript) {
        return "{" +
            "\"python\":" + jsonEscape(python) + "," +
            "\"java\":" + jsonEscape(java) + "," +
            "\"javascript\":" + jsonEscape(javascript) +
            "}";
    }

    private static String testCases(String... cases) {
        return "[" + String.join(",", cases) + "]";
    }

    private static String tc(String input, String expectedOutput, boolean hidden) {
        return "{\"input\":" + jsonEscape(input) +
            ",\"expected_output\":" + jsonEscape(expectedOutput) +
            ",\"hidden\":" + hidden + "}";
    }

    /** Minimalist JSON string escaper (handles newlines, quotes, backslashes). */
    private static String jsonEscape(String s) {
        return "\"" + s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            + "\"";
    }
}
