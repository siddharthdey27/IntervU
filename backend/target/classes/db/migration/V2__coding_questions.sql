-- V2: Coding questions module (question bank + submissions)
-- NOTE: If you already ran V1 which created coding_questions with UUID, drop it first:
--   DROP TABLE IF EXISTS coding_submissions CASCADE;
--   DROP TABLE IF EXISTS coding_questions CASCADE;

CREATE TABLE IF NOT EXISTS coding_questions (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    difficulty      VARCHAR(20)  NOT NULL CHECK (difficulty IN ('EASY','MEDIUM','HARD')),
    category        VARCHAR(100),
    description     TEXT NOT NULL,
    boilerplate     JSONB NOT NULL DEFAULT '{}',   -- { "java": "...", "python": "...", "javascript": "..." }
    test_cases      JSONB NOT NULL DEFAULT '[]',   -- [ { "input": "...", "expected_output": "...", "hidden": false } ]
    time_limit_ms   INTEGER NOT NULL DEFAULT 2000,
    memory_limit_kb INTEGER NOT NULL DEFAULT 128000,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS code_submissions (
    id                BIGSERIAL PRIMARY KEY,
    user_id           VARCHAR(36) NOT NULL,           -- stores UUID string from JWT
    question_id       BIGINT NOT NULL REFERENCES coding_questions(id) ON DELETE CASCADE,
    language          VARCHAR(30) NOT NULL,
    source_code       TEXT NOT NULL,
    status            VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, ACCEPTED, WRONG_ANSWER, ERROR, TIMEOUT
    passed_test_count INTEGER NOT NULL DEFAULT 0,
    total_test_count  INTEGER NOT NULL DEFAULT 0,
    stdout            TEXT,
    stderr            TEXT,
    execution_time_ms INTEGER,
    memory_kb         INTEGER,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_code_submissions_user ON code_submissions(user_id);
CREATE INDEX IF NOT EXISTS idx_code_submissions_question ON code_submissions(question_id);

-- A few starter questions
INSERT INTO coding_questions (title, difficulty, category, description, boilerplate, test_cases)
VALUES
(
  'Two Sum',
  'EASY',
  'Arrays',
  'Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target. Read input as a line of space-separated integers, then the target on the next line. Print the two indices space-separated.',
  '{"java": "import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    // read nums line, then target\n  }\n}", "python": "nums = list(map(int, input().split()))\ntarget = int(input())\n# your code here", "javascript": "const lines = require(\"fs\").readFileSync(0, \"utf8\").split(\"\\n\");\nconst nums = lines[0].trim().split(\" \").map(Number);\nconst target = parseInt(lines[1]);\n// your code here"}',
  '[{"input": "2 7 11 15\n9", "expected_output": "0 1", "hidden": false}, {"input": "3 2 4\n6", "expected_output": "1 2", "hidden": true}]'
),
(
  'Reverse String',
  'EASY',
  'Strings',
  'Given a string, reverse it and print the result. Read the string from standard input.',
  '{"java": "import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    Scanner sc = new Scanner(System.in);\n    String s = sc.nextLine();\n    // your code here\n  }\n}", "python": "s = input()\n# your code here", "javascript": "const s = require(\"fs\").readFileSync(0, \"utf8\").trim();\n// your code here"}',
  '[{"input": "hello", "expected_output": "olleh", "hidden": false}, {"input": "OpenAI", "expected_output": "IAnepO", "hidden": false}, {"input": "racecar", "expected_output": "racecar", "hidden": true}]'
),
(
  'FizzBuzz',
  'EASY',
  'Logic',
  'Print numbers from 1 to n. For multiples of 3 print "Fizz", for multiples of 5 print "Buzz", for multiples of both print "FizzBuzz". Read n from standard input. Print one value per line.',
  '{"java": "import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    int n = Integer.parseInt(new Scanner(System.in).nextLine().trim());\n    // your code here\n  }\n}", "python": "n = int(input())\n# your code here", "javascript": "const n = parseInt(require(\"fs\").readFileSync(0, \"utf8\").trim());\n// your code here"}',
  '[{"input": "5", "expected_output": "1\n2\nFizz\n4\nBuzz", "hidden": false}, {"input": "15", "expected_output": "1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\nBuzz\n11\nFizz\n13\n14\nFizzBuzz", "hidden": true}]'
),
(
  'Valid Parentheses',
  'MEDIUM',
  'Stacks',
  'Given a string containing just the characters ''('', '')'', ''{'', ''}'', ''['' and '']'', determine if the input string is valid. Print "true" or "false". An input string is valid if: open brackets are closed by the same type of brackets, and open brackets are closed in the correct order.',
  '{"java": "import java.util.*;\npublic class Main {\n  public static void main(String[] args) {\n    String s = new Scanner(System.in).nextLine().trim();\n    // your code here\n  }\n}", "python": "s = input().strip()\n# your code here", "javascript": "const s = require(\"fs\").readFileSync(0, \"utf8\").trim();\n// your code here"}',
  '[{"input": "()", "expected_output": "true", "hidden": false}, {"input": "()[]{}", "expected_output": "true", "hidden": false}, {"input": "(]", "expected_output": "false", "hidden": false}, {"input": "([)]", "expected_output": "false", "hidden": true}]'
)
ON CONFLICT DO NOTHING;
