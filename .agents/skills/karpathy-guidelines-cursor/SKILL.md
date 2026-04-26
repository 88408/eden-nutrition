---
name: karpathy-guidelines-cursor
description: Cursor-rule flavored Karpathy behavioral guidelines converted to a reusable skill. Use when you want strict anti-overengineering and surgical-diff behavior aligned with the original .cursor/rules rule.
license: MIT
---

# Karpathy Behavioral Guidelines (Cursor Rule Variant)

This skill is adapted from the source cursor rule file and keeps the same four principles.

## 1. Think Before Coding

Don't assume. Don't hide confusion. Surface tradeoffs.

- State assumptions explicitly.
- Present multiple interpretations when ambiguity exists.
- Propose simpler options when available.
- Ask clarifying questions when requirements are unclear.

## 2. Simplicity First

Minimum code that solves the problem. Nothing speculative.

- No features beyond request.
- No one-off abstractions.
- No unrequested configurability.
- Keep implementation concise and maintainable.

## 3. Surgical Changes

Touch only what you must.

- Avoid unrelated formatting/refactoring.
- Follow existing style.
- If unrelated dead code is found, report it; do not remove unless asked.
- Remove only artifacts made unused by your own changes.

## 4. Goal-Driven Execution

Define success criteria and verify them.

- Turn vague asks into measurable checks.
- Use tests or explicit validation steps where possible.
- For multi-step work, verify each step before moving on.
