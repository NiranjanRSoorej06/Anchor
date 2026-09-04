# Anchor

## Project

Anchor is an Android-first PTSD support application being developed for a
36-hour hackathon.

The goal is to provide discreet, low-cognitive-load support during and after
high-stress or trauma-related episodes.

The project is for the "Inclusive Innovation: Access Without Limits" track,
which focuses on technologies that improve quality of life, independence,
comfort, communication, and support for people living with neurological and
psychological conditions.

## Core Product Philosophy

Anchor is designed around the possibility that during acute distress a user
may have difficulty with attention, fine motor control, reading, decision
making, and navigating a conventional application.

Therefore prioritize:

- Minimal cognitive load
- Minimal interaction
- Very fast response
- Discreet operation
- Privacy
- Offline-first functionality where practical
- Reliability over feature count
- Simple, demonstrable implementations
- Dignity and non-stigmatizing UX

## Current Product Concepts

The team is currently considering:

1. Somatic Textures / Digital Worry Stone
   - Custom Android haptic patterns intended to provide a tactile grounding
     experience.
   - Candidate patterns: Purr, Heartbeat, Marble.

2. PTSD Hangover / Refractory Mode
   - A post-event recovery state lasting approximately 12 hours.
   - Potentially changes the interface and provides recovery-oriented prompts.

3. Prefrontal Bypass / Hardware Trigger
   - A low-cognition physical trigger, currently envisioned around repeated
     volume-button interaction.

4. Vibrotactile Breathing / Grounding
   - Haptic patterns synchronized with breathing.

5. Dynamic Sensory Gating
   - Potential use of generated/masked audio such as brown noise to reduce
     distracting environmental sound.

6. Environmental Sound / Reality Check
   - Potentially identify environmental sounds and provide a discreet
     contextual audio response.

7. Emergency / Trusted Contact
   - Allow a user to discreetly signal a trusted person during distress.

8. Passive EMA / Private Pattern Log
   - Passively record useful contextual information locally to identify
     patterns over time.

9. Self-Referential Auditory Anchoring
   - A user-recorded personal grounding/safety phrase.

These are candidate features, NOT confirmed requirements.

## Hackathon Constraints

This is a 36-hour hackathon.

Optimize for:

1. A reliable working MVP
2. A compelling demonstration
3. Technical feasibility
4. Low implementation complexity
5. Minimal dependencies
6. Minimal external infrastructure
7. Privacy
8. Offline functionality where practical

Do not attempt to build every proposed feature.

A smaller number of polished, reliable features is preferable to many
partially working features.

Every risky feature should have a simpler fallback or be removable without
breaking the MVP.

## Technical Direction

The application is Android-first.

Prefer native Android capabilities when they provide a simpler or more
reliable implementation.

Do not introduce a backend, cloud service, AI API, or third-party dependency
unless there is a clear reason it improves the MVP.

Potentially sensitive user information should remain local whenever practical.

## Development Rules

Before implementing a substantial feature:

1. Inspect the existing codebase.
2. Understand the relevant architecture.
3. Reuse existing functionality where possible.
4. Prefer the simplest viable implementation.
5. Avoid unnecessary dependencies.
6. Avoid unrelated refactoring.
7. Do not silently change approved architectural decisions.
8. Test the implementation before moving to the next major task.

## Planning Rules

For substantial features, use Plan Mode first.

Plans should include:

- What is being built
- Why it is needed
- Files/components affected
- Dependencies
- Data flow
- Android APIs involved
- Permissions required
- Technical risks
- Testing strategy
- Hackathon fallback

Do not implement a substantial feature before the plan has been reviewed
and approved.

## Model / Token Efficiency

Do not use an expensive reasoning model for routine implementation.

Use stronger reasoning for:

- Architecture
- Technical feasibility
- Difficult Android APIs
- Hardware interaction
- Audio processing
- Security/privacy decisions
- Difficult debugging
- Code review

Use faster/cheaper models for:

- Straightforward UI
- Boilerplate
- Simple CRUD
- Styling
- Documentation
- Mechanical refactoring
- Simple tests

Keep implementation tasks small and independently testable.

## Scope Control

Do not expand the project beyond the approved MVP.

If a feature is technically risky or likely to consume substantial hackathon
time, explain the risk and propose a simpler alternative before implementing it.

Never assume that a research claim means the corresponding technical feature
is clinically validated.

Anchor is a hackathon prototype, not a medical device or replacement for
professional care.
