# Compiler makefile

# Sources files
LABSRC    = $(filter-out src/javalette/*.java, $(wildcard src/*.java src/**/*.java))
LABSRCDIR = src
LABSRCOUT = src/javalette

# Object files
PARSERSRC = $(wildcard src/javalette/Absyn/*.java src/javalette/*.java)
PARSEROBJ = $(PARSERSRC:.java=.class)
LABOBJ    = $(LABSRC:.java=.class)

# Name of generated .cup file for bnfc ≥ 2.8.1
CUPFILE = src/javalette/_cup.cup

JAVAC       = javac
JAVAC_FLAGS = -sourcepath ./src -d ./build
JAVA        = java

javac       = ${JAVAC} ${JAVAC_FLAGS}
java        = ${JAVA}  ${JAVA_FLAGS}

# Global rules
################################################################################

# Goals not corresponding to file names
.PHONY: default all clean distclean vclean

# Default goal, needs to remain first
default: build/fr/rthd/jlc/Main.class

# Build and ship
all: default sdist

# Compiling Main class
################################################################################

build/fr/rthd/jlc/Main.class: src/fr/rthd/jlc/Main.java build/javalette/Test.class
	$(javac) $<

# Compiling the parser
################################################################################

# Create parser source via bnfc
build/javalette/Yylex $(CUPFILE) src/javalette/Test.java: src/javalette.cf
	bnfc --java -o src $<
	# Backward compatibility for bnfc < 2.8.1
	cp src/javalette/javalette.cup $(CUPFILE) || true

# Create parser and move it to the correct location
src/javalette/parser.java src/javalette/sym.java: $(CUPFILE)
	# FIXME: The BNFC grammar contains an unresolved conflict near Constructor
	$(java) java_cup.Main -expect 2 -package javalette $<
	mv parser.java sym.java src/javalette/

# Create lexer
src/javalette/Yylex.java: src/javalette/Yylex
	$(java) JLex.Main $<

# Compile lexer
build/javalette/Yylex.class: src/javalette/Yylex.java build/javalette/sym.class
	$(javac) $<

# Create parser test
build/javalette/Test.class: src/javalette/Test.java build/javalette/parser.class build/javalette/sym.class build/javalette/Yylex.class
	$(javac) $<

# Create parser
build/javalette/parser.class: src/javalette/parser.java
	$(javac) $<

# Sym
build/javalette/sym.class: src/javalette/sym.java
	$(javac) $<

# Default rule for all source files
################################################################################

build/%.class: src/%.java build/javalette/Test.class
	$(javac) $<

# Rules for shipping the solution
################################################################################

# Create distribution
sdist: submission.tar.gz

# Create submission zip
submission.tar.gz: Makefile
	$(eval tmpdir := $(shell mktemp -d))
	mkdir $(tmpdir)/submission
	cp -r $(LABSRCDIR) $(tmpdir)/submission
	rm -r $(tmpdir)/submission/$(LABSRCOUT)
	cp jlc* $(tmpdir)/submission
	mkdir $(tmpdir)/submission/doc
	cp README.md $(tmpdir)/submission/doc
	cp -r lib $(tmpdir)/submission
	cp $^ $(tmpdir)/submission/
	cd $(tmpdir)/submission && tar -czhf $@ *
	mv $(tmpdir)/submission/$@ .

# Rules for cleaning generated files
################################################################################

# Clean all output files
clean:
	-rm -rf build

# Clean all generated files
vclean: clean
	-rm -rf $(LABSRCOUT)
	-mkdir $(LABSRCOUT)
	touch $(LABSRCOUT)/.gitkeep

# Clean everything
distclean: vclean
	-rm -f submission.tar.gz

# EOF
