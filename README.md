# Akka persistence typed 

## Introduction

A persistent actor is an event sourced actor. It receives a (non-persistent) command which is first validated. 

If validation succeeds, events are generated from the command, representing the effect of the command. These events are then persisted and used to change the actor’s state. 

When the event sourced actor needs to be recovered, only the persisted events are replayed as they can be successfully applied to recreate the state of the actor. 

Event sourced actors may also process commands that do not change application state (such as query commands).

---
