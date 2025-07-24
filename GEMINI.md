# GEMINI.md - Blue Light Filter Project

##  **ACT AS SENIOR ANDROID ENGINEER**

##  **READ FIRST: Check `doc/` folder for complete project requirements**

## Architecture
- **MVVM**: Data + UI layers only (no domain layer)
- **Tech Stack**: Kotlin + XML + Activity-based
- **Focus**: Reusable components

## Requirements
1. **Refactor codebase** with reusable components
2. **MVVM structure**: ViewModels, Activities, Repositories
3. **Clean Kotlin code** with proper lifecycle management
4. **Modular components** for UI and logic
5. **Blue light filter functionality** optimization

## Project Structure
```
app/src/main/java/
├── data/ (repositories, models)
├── ui/ (activities, viewmodels, adapters)
└── util/ (reusable components)
```

## Checklist
- [ ] MVVM implementation
- [ ] Reusable components created
- [ ] Activity lifecycle proper handling
- [ ] Kotlin best practices
- [ ] Blue light filter optimized
- [ ] Memory leak prevention

## ⚡ **DEVELOPMENT WORKFLOW**
**After each feature completion:**
1. Run `./gradlew build` or `gradle build`
2. Fix all compilation errors before proceeding
3. Ensure clean builds throughout development

**Simple project - focus on core functionality with clean architecture.**