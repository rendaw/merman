<p align="center"><img src="logo.png" width="95%" alt="merman logo"></p>

Just what _is_ merman?

Traditionally, a compiler or interpreter has two main parts: the parser and backend (generator or executor).  The parser transforms human readable text (the source code) into an abstract syntax tree (AST), and the backend outputs machine code or executes the AST.

![Example #1](readme/diagram_editor.svg)

The editor operates on text, a series of bytes, which brings out difficulties:

- It is easy to make edits that break the syntax.  For example, accidentally deleting part of a keyword or an overreaching replace corrupting type names.
- Navigation makes no distinction between grammatic elements.  The cursor may end up in the middle of a word when trying to rearrange statements, jump over complex operator chains, or not jump far enough when dealing with long variable names and strings.

Some editors may include a parser to provide the editor access to the AST.  While this allows the editor to make smarter edits and colorize the text, it has some of its own drawbacks:

- A parser takes significant development resources, and a parser is needed for each supported language.
- The parser needs to be kept up to date with the compiler or risk losing significant functionality, getting in the way of work.
- Bidirectional synchronization between AST and text is complex and fragile.  Typos may make the AST unavailable or make it wildly misinterpret the code.
- Users judge languages and fight over things like whitespace decisions and block delimiters.

Using a series of bytes as source code also presents problems on the compiler side:

- To create a new language need to create a parser
- To create a parser you need to decide on a syntax
- New features need new syntax constructs.  Old syntax constructs may get in the way.  Features may be compromised for the sake of syntax usability.
- Even trivial syntax changes may break compatibility.  Updating source code is often manual since automated changes may be incorrect and errors may make the source nonsensical and uncompilable.

And debugging:

- Breakpoints and tracebacks are usually line based, when a single line may consist of dozens of clauses.  Sometimes breakpoints and tracebacks are statement based, which is even less precise.
- Where grammatic elements begin and end is not always clear, making code flow hard to understand when stepping.

merman is an editor that operates directly on the AST in the hopes of simplifying much of the above.

![Example #1](readme/diagram_merman.svg)

merman does have something like a reverse-parser to visualize the AST in a text syntax-like manner, but it is conceptually simple.

merman aims to provide these benefits:

- No syntax errors.
- Open the road to more powerful navigation - accessibility.
- Iterate new language development faster by debinding the syntax from the AST.
- Languages can use color, font, and images to distinguish elements of the grammar.
- The user can explicitly resolve ambiguity when it arises, rather than leaving it to the compiler to guess or give up.
- AST manipulation in scripts and other programs is easy and safe.
- Users can customize the syntax and add sugars to suit their workflow.
- Programming languages can be localized.

Of course, there are some drawbacks to this approach

- It's a very different way of thinking and interacting with source code.
- Communicating about source code is more difficult, especially if users customize their syntax.
- Displaying source code on the web is more difficult.
- merman is a minimal, rough proof of concept written in Java.

# Get it

## Installer

## Jar

Requires Java 9.

link

run with

## Build from source

Need to maven install these

Then in this project

mvn package

java -jar standalone/target/standalone.jar

# Documentation

- Create a language in 10 minutes
- Syntax reference
- Action reference
- Concepts
