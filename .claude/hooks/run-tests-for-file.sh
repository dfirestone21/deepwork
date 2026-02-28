#!/usr/bin/env bash
# PostToolUse hook: runs the specific test class for the file being edited.
# Reads the edited file path from the hook JSON payload, derives the test class,
# picks the right Gradle task based on source set, and skips silently if no test exists.

PAYLOAD=$(cat)

# Extract file path from hook payload
FILE_PATH=$(echo "$PAYLOAD" | jq -r '.tool_input.path // empty')

# Only process Kotlin/Java source files
if [[ ! "$FILE_PATH" =~ \.(kt|java)$ ]]; then
    exit 0
fi

# Only process files inside a recognised source set
if [[ "$FILE_PATH" =~ /src/main/ ]]; then
    SOURCE_SET="main"
elif [[ "$FILE_PATH" =~ /src/pruvan/ ]]; then
    SOURCE_SET="pruvan"
elif [[ "$FILE_PATH" =~ /src/ppw/ ]]; then
    SOURCE_SET="ppw"
else
    exit 0
fi

# Derive package and class name from path
# e.g. .../src/main/java/com/pruvan/mobile/usecase/MyUseCaseImpl.kt
JAVA_RELATIVE=$(echo "$FILE_PATH" | sed 's|.*src/[^/]*/java/||')
# → com/pruvan/mobile/usecase/MyUseCaseImpl.kt

CLASS_FILE=$(basename "$JAVA_RELATIVE")
CLASS_NAME="${CLASS_FILE%.*}"       # MyUseCaseImpl
TEST_CLASS="${CLASS_NAME}Test"      # MyUseCaseImplTest

PACKAGE_PATH=$(dirname "$JAVA_RELATIVE")        # com/pruvan/mobile/usecase
PACKAGE=$(echo "$PACKAGE_PATH" | tr '/' '.')    # com.pruvan.mobile.usecase
FULL_TEST_CLASS="${PACKAGE}.${TEST_CLASS}"       # com.pruvan.mobile.usecase.MyUseCaseImplTest

PROJECT_DIR="$CLAUDE_PROJECT_DIR"

# Check if a test file exists for this class anywhere in the project
TEST_FILE=$(find "$PROJECT_DIR" \
    \( -path "*/src/test/java/${PACKAGE_PATH}/${TEST_CLASS}.kt" \
    -o -path "*/src/test/java/${PACKAGE_PATH}/${TEST_CLASS}.java" \
    -o -path "*/src/testPruvan/java/${PACKAGE_PATH}/${TEST_CLASS}.kt" \
    -o -path "*/src/testPruvan/java/${PACKAGE_PATH}/${TEST_CLASS}.java" \
    -o -path "*/src/testPpw/java/${PACKAGE_PATH}/${TEST_CLASS}.kt" \
    -o -path "*/src/testPpw/java/${PACKAGE_PATH}/${TEST_CLASS}.java" \
    \) 2>/dev/null | head -1)

# No test file found — skip silently
if [[ -z "$TEST_FILE" ]]; then
    exit 0
fi

# Pick Gradle task(s) based on source set
case "$SOURCE_SET" in
    main)
        # Changes to shared code could affect both flavors
        TASKS="testPruvanDebugUnitTest testPpwDebugUnitTest"
        ;;
    pruvan)
        TASKS="testPruvanDebugUnitTest"
        ;;
    ppw)
        TASKS="testPpwDebugUnitTest"
        ;;
esac

cd "$PROJECT_DIR"
./gradlew $TASKS --tests "$FULL_TEST_CLASS" 2>&1 | tail -30
