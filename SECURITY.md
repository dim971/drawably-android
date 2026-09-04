# Security policy

## Supported versions

This library is pre-1.0. Fixes land on `main` and go out in the next tagged
release; there are no maintained branches for older tags yet.

## Reporting a vulnerability

Please **do not** open a public issue for a security problem.

Use GitHub's private reporting instead: go to the **Security** tab of this
repository and choose **Report a vulnerability**. That opens a private advisory
visible only to the maintainers.

Please include what you were doing, what happened, and how to reproduce it. You
can expect an acknowledgement within a few days, and to be kept informed until
it is resolved.

## Scope

This is a drawing library with no network access, no persistence, no
credentials and no dependencies. The plausible surface is small: input that
makes the geometry misbehave — a size, a seed or a theme value that causes a
crash, a hang, or unbounded memory use. Those are worth reporting.
