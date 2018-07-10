**Note** Unfortunately, I was unable to recover the source code of the actual ASM transformers that I put together, but I hope to find it lying around somewhere soon. Currently, the binary of the compiled bytecode transformer system can be found at `src/data.jar`.

# AttackIndicatorMod
A Minecraft mod installer for an improved cooldown attack indicator.

# How does this work?

ASM! ASM! ASM! I love ASM!

ASM is a lightweight Java bytecode injection library which my mod utilizes to be able to *inject* code into Minecraft classes, rather than overwriting them -- in effect, this mod is be able to work on almost any Minecraft version, regardless of other mods installed, such as Forge, 5zig, and OptiFine.

After studying the semantics of Java Bytecode, I put together a transformer system that edits bytecode of only specific classes, leaving the rest of the Minecraft JAR untouched and allowing for other mods to be present as well.

When Minecraft launches, AttackIndicatorFix injects into the ClassLoader, allowing it to rewrite classes at *runtime* rather than compiletime. In effect, this mod is very dynamic and versatile.

Hope you enjoy!
