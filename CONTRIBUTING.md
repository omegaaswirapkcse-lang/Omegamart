# OmegaMart Contribution Guidelines (CONTRIBUTING.md)

## Development Workflow
1. Follow standard Java naming conventions and clean code guidelines.
2. Ensure every new database query uses `PreparedStatement`.
3. Add unit tests for service-layer methods using JUnit 5 and Mockito.
4. Execute `mvn checkstyle:check` and `mvn spotbugs:check` before submitting pull requests.

## Commit Message Convention
Use conventional commits:
- `feat:` for new features
- `fix:` for bug fixes
- `test:` for test additions or updates
- `docs:` for documentation updates
- `refactor:` for code restructuring without behavioral changes
