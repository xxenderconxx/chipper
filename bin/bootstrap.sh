#!/bin/bash -f

if [ `uname` == "Linux" ] ; then
  flags=""
  platform="linux"
elif [ `uname` == "Darwin" ] ; then
  flags=""
  platform="os-x"
else 
  flags=""
  platform="unknown"
fi

stanza3 ~/stanza3/core/reader.stanza \
         ~/stanza3/core/macro-utils.stanza \
         ~/stanza3/compiler/stz-algorithms.stanza \
         ~/stanza3/compiler/stz-padder.stanza \
         ~/stanza3/compiler/stz-utils.stanza \
         ~/stanza3/compiler/stz-parser.stanza \
         ~/stanza3/compiler/stz-params.stanza \
         ~/stanza3/compiler/stz-core-macros.stanza \
         ~/stanza3/compiler/stz-ids.stanza \
         ~/stanza3/compiler/stz-imms.stanza \
         ~/stanza3/compiler/stz-tgt-writer.stanza \
         ~/stanza3/compiler/lang-read.stanza \
         ~/stanza3/compiler/lang-check.stanza \
         ~/stanza3/compiler/stz-primitives.stanza \
         ~/stanza3/compiler/stz-pkg-ir.stanza \
         ~/stanza3/compiler/stz-il-ir.stanza \
         ~/stanza3/compiler/stz-tl-ir.stanza \
         ~/stanza3/compiler/stz-kl-ir.stanza \
         ~/stanza3/compiler/stz-tgt-ir.stanza \
         ~/stanza3/compiler/stz-bb-ir.stanza \
         ~/stanza3/compiler/stz-asm-ir.stanza \
         ~/stanza3/compiler/stz-backend.stanza \
         ~/stanza3/compiler/stz-input.stanza \
         ~/stanza3/compiler/stz-namemap.stanza \
         ~/stanza3/compiler/stz-renamer.stanza \
         ~/stanza3/compiler/stz-resolver.stanza \
         ~/stanza3/compiler/stz-infer.stanza \
         ~/stanza3/compiler/stz-type-calculus.stanza \
         ~/stanza3/compiler/stz-type.stanza \
         ~/stanza3/compiler/stz-kform.stanza \
         ~/stanza3/compiler/stz-tgt.stanza \
         ~/stanza3/compiler/stz-bb.stanza \
         ~/stanza3/compiler/stz-asm-emitter.stanza \
         ~/stanza3/compiler/stz-compiler.stanza \
         ~/stanza3/compiler/stz-arg-parser.stanza \
         ~/stanza3/compiler/stz-langs.stanza  \
         ~/stanza3/compiler/lang-renamer.stanza \
         ~/stanza3/compiler/lang-resolver.stanza \
         src/chipper-syntax.stanza \
         ~/stanza3/compiler/stz-main.stanza \
      -o bin/chipper \
      -optimize

