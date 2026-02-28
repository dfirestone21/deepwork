#!/usr/bin/env bash
# PostToolUse hook: runs the specific test class for the file being edited.
# Reads the edited file path from the hook JSON payload, derives the test class,
# and skips silently if no test exists.

PAYLOAD=$(cat)

# Extract file path from hook payload
FILE_PATH=$(echo "$PAYLOAD" | jq -r '.tool_input.path // empty')

# Only process Kotlin/Java source files
if [[ ! "$FILE_PATH" =~ \.(kt|java)$ ]]; then
    exit 0
fi

# Only process files inside the main source set
if [[ ! "$FILE_PATH" =~ /src/main/ ]]; then
    exit 0
fi

# Derive package and class name from path
# e.g. .../src/main/java/com/example/deepwork/usecase/MyUseCase.kt
JAVA_RELATIVE=$(echo "$FILE_PATH" | sed 's|.*src/main/java/||')
# → com/example/deepwork/usecase/MyUseCase.kt

CLASS_FILE=$(basename "$JAVA_RELATIVE")
CLASS_NAME="${CLASS_FILE%.*}"       # MyUseCase
TEST_CLASS="${CLASS_NAME}Test"      # MyUseCaseTest

PACKAGE_PATH=$(dirname "$JAVA_RELATIVE")        # com/example/deepwork/usecase
PACKAGE=$(echo "$PACKAGE_PATH" | tr '/' '.')    # com.example.deepwork.usecase
FULL_TEST_CLASS="${PACKAGE}.${TEST_CLASS}"       # com.example.deepwork.usecase.MyUseCaseTest

PROJECT_DIR="$CLAUDE_PROJECT_DIR"

# Check if a test file exists for this class anywhere in the project
TEST_FILE=$(find "$PROJECT_DIR" \
    \( -path "*/src/test/java/${PACKAGE_PATH}/${TEST_CLASS}.kt" \
    -o -path "*/src/test/java/${PACKAGE_PATH}/${TEST_CLASS}.java" \
    \) 2>/dev/null | head -1)

# No test file found — skip silently
if [[ -z "$TEST_FILE" ]]; then
    exit 0
fi

cd "$PROJECT_DIR"
./gradlew test --tests "$FULL_TEST_CLASS" 2>&1 | tail -30
