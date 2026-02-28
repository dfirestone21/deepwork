#!/usr/bin/env bash
# .claude/hooks/skill-eval.sh
# Injected before every prompt via UserPromptSubmit hook.
# Forces Claude to evaluate available skills before responding,
# increasing TDD skill activation from ~20% to ~84%.

# Consume stdin (required by Claude Code hook protocol)
cat /dev/stdin > /dev/null

cat <<'EOF'
INSTRUCTION: MANDATORY SKILL ACTIVATION SEQUENCE

Step 1 — EVALUATE:
For each skill listed in <available_skills>, state on one line:
  [skill-name] - YES or NO - [one-line reason]

Step 2 — ACTIVATE:
  IF any skill is YES → call the Skill(skill-name) tool for EACH relevant skill NOW, before doing anything else.
  IF no skill is YES → state "No skills needed" and proceed.

Step 3 — IMPLEMENT:
  Only after Step 2 is complete, proceed with the response or implementation.

CRITICAL: You MUST complete Steps 1 and 2 before writing any code or making any file changes.
Do NOT skip straight to implementation.
EOF
