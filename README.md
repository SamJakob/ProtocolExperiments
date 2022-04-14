# ProtocolExperiments
Experiments with data (en/de)coding, etc., for protocols.

- [interfaces/](./src/main/java/com/samjakob/protocol_experiments/interfaces) -
  are intended for making the examples more re-usable by acting as stubs that
  can be easily replaced with a proper implementation. Refer to the individual
  files for more details.
- [utils/](./src/main/java/com/samjakob/protocol_experiments/utils) -
  contains utilities common to many of the other example files intended for
  testing the experiments.

## Current Experiments

- [VarLengthNumbers](./src/main/java/com/samjakob/protocol_experiments/data/VarLengthNumbers.java) -
  an implementation of variable length numbers that ended up being essentially
  the same as the Minecraft implementation of variable length numbers.
  - Refer to https://wiki.vg/Protocol#VarInt_and_VarLong for more information.
