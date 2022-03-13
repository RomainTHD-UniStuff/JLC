# Makefile for PLT lab 3 in JAVA

# Variables for file lists
###########################################################################

# Edit to add new java source files, if needed!
LABSRC    = src/jlc.java \
			src/Env.java \
			src/ExpCustom.java \
			src/Runtime.java \
			src/TypeChecker.java \
			src/TypeCode.java \
			src/TypeException.java
LABSRCDIR = src
LABSRCOUT = src/javalette

# No need to edit these:
PARSERSRC = $(wildcard src/javalette/Absyn/*.java src/javalette/*.java)
PARSEROBJ = $(PARSERSRC:.java=.class)
LABOBJ    = $(LABSRC:.java=.class)
# Inner classes:
# LABINNOBJ = $(wildcard $(LABSRC:.java=$$*.class))

# Variables for the programming environment (edit as needed)
###########################################################################

# Name of generated .cup file for bnfc ≥ 2.8.1
CUPFILE = src/javalette/javalette.cup

JAVAC       = javac
JAVAC_FLAGS = -sourcepath ./src -d ./build
JAVA        = java

# No need to edit these:
javac       = ${JAVAC} ${JAVAC_FLAGS}
java        = ${JAVA}  ${JAVA_FLAGS}

# Default rule
###########################################################################

# List of goals not corresponding to file names.
.PHONY : default all clean distclean vclean

# As the first goal is default goal, this goal needs to remain first.
default : build/jlc.class build/Runtime.class

# Build and ship
all : default sdist

# Rules for compiling Main classes (modify or add as needed)
###########################################################################

build/TypeChecker.class : src/TypeChecker.java build/TypeException.class build/javalette/Test.class
	$(javac) $<

# build/Compiler.class : src/Compiler.java build/javalette/Test.class
# 	$(javac) $<

# build/jlc.class : src/jlc.java build/TypeChecker.class build/Compiler.class build/javalette/Test.class
# 	$(javac) $<

build/jlc.class : src/jlc.java build/TypeChecker.class build/javalette/Test.class
	$(javac) $<


# Rules for creating the parser
###########################################################################

# Create parser source via bnfc (dependency javalette.cf needs to be first).
# Patch javalette/Absyn/Exp.java
build/javalette/Yylex $(CUPFILE) src/javalette/Test.java : javalette.cf
	bnfc --java -o src $<

# Create parser and move it to the correct location.
src/javalette/parser.java src/javalette/sym.java : $(CUPFILE)
	# FIXME: The BNFC grammar contains a conflict, which is not resolved.
	$(java) java_cup.Main -expect 1 -package javalette $<
	mv parser.java sym.java src/javalette/

# Create lexer.
src/javalette/Yylex.java : src/javalette/Yylex
	$(java) JLex.Main $<

# Compile lexer.
build/javalette/Yylex.class : src/javalette/Yylex.java build/javalette/sym.class
	$(javac) $<

# Create parser test.
build/javalette/Test.class : src/javalette/Test.java build/javalette/parser.class build/javalette/sym.class build/javalette/Yylex.class
	$(javac) $<

# Create parser.
build/javalette/parser.class : src/javalette/parser.java
	$(javac) $<

# Sym
build/javalette/sym.class : src/javalette/sym.java
	$(javac) $<

# Default rules
###########################################################################

build/%.class : src/%.java build/javalette/Test.class
	$(javac) $<


# Rules for shipping the solution
###########################################################################

sdist : createTmp Main.tar.gz

tmpdir := $(shell mktemp -d)
createTmp :
	mkdir $(tmpdir)/Main
	cp -r $(LABSRCDIR) $(tmpdir)/Main/
	rm -r $(tmpdir)/Main/$(LABSRCOUT)

Main.tar.gz : javalette.cf Makefile Main.hs Main.sh Main.bat
	cp $^ $(tmpdir)/Main/
	cp lib/jasmin.jar $(tmpdir)/Main/
	tar -C $(tmpdir) -czhf $@ Main

# Rules for cleaning generated files
###########################################################################

clean :
	-rm -r build
	-mkdir build
	-touch build/.gitkeep
	-rm -f javalette.dvi javalette.aux javalette.log javalette.ps
	-rm -f $(LABOBJ)
	-rm -f Main.hi Main.o Main.exe Main
# Uncomment to also remove all .class files in current directory
#	-rm -f *.class

vclean : clean
	-rm -f $(PARSERSRC)
	-rm -f src/javalette/Absyn/*.bak src/javalette/*.bak
	-rmdir src/javalette/Absyn/
	-rm -f src/javalette.tex
	-rm -f src/javalette/Yylex $(CUPFILE)
	touch src/javalette/.gitkeep

distclean : vclean
	-rm -f Main.tar.gz

# Debugging the Makefile
###########################################################################

debug :
	echo $(LABINNOBJ)

# EOF
