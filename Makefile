# Compiler makefile

# Recursive wildcard expansion
rwildcard = $(foreach d,$(wildcard $(1:=/*)),$(call rwildcard,$d,$2) $(filter $(subst *,%,$2),$d))

# Sources files
BNFC_SRC_DIR = generated/javalette
BNFC_SRC     = $(call rwildcard,$(BNFC_SRC_DIR),*.java)
SRC_DIR      = src
BUILD_DIR    = build
SRC          = $(call rwildcard,$(SRC_DIR)/fr/rthd,*.java)
OBJ          = $(subst $(SRC_DIR)/,$(BUILD_DIR)/,$(SRC:.java=.class))

# Name of generated .cup file for bnfc ≥ 2.8.1
CUPFILE = generated/javalette/_cup.cup

JAVAC                 = javac
JAVAC_SRC_FLAGS       = -cp *:lib/*:. -sourcepath "./src:./generated"       -d ./build
JAVAC_JAVALETTE_FLAGS = -cp *:lib/*:. -sourcepath ./generated -d ./build
JAVA                  = java

javac_src             = ${JAVAC} ${JAVAC_SRC_FLAGS}
javac_javalette       = ${JAVAC} ${JAVAC_JAVALETTE_FLAGS}
java                  = ${JAVA}  ${JAVA_FLAGS}

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
	$(javac_src) $<

# Compiling the parser
################################################################################

# Create parser source via bnfc
build/javalette/Yylex $(CUPFILE) generated/javalette/Test.java: src/javalette.cf
	bnfc --java -o generated $<
	# Backward compatibility for bnfc < 2.8.1
	cp generated/javalette/javalette.cup $(CUPFILE) || true

# Create parser and move it to the correct location
generated/javalette/parser.java generated/javalette/sym.java: $(CUPFILE)
	$(java) java_cup.Main -expect 3 -package javalette $<
	mv parser.java sym.java generated/javalette/

# Create lexer
generated/javalette/Yylex.java: generated/javalette/Yylex
	$(java) JLex.Main $<

# Compile lexer
build/javalette/Yylex.class: generated/javalette/Yylex.java build/javalette/sym.class
	$(javac_javalette) $<

# Create parser test
build/javalette/Test.class: generated/javalette/Test.java build/javalette/parser.class build/javalette/sym.class build/javalette/Yylex.class
	$(javac_javalette) $<

# Create parser
build/javalette/parser.class: generated/javalette/parser.java
	$(javac_javalette) $<

# Sym
build/javalette/sym.class: generated/javalette/sym.java
	$(javac_javalette) $<

# Default rule for all source files
################################################################################

build/%.class: src/%.java build/javalette/Test.class
	$(javac_src) $<

# Rules for shipping the solution
################################################################################

# Create distribution
sdist: submission.tar.gz

# Create submission zip
submission.tar.gz: Makefile
	$(eval tmpdir := $(shell mktemp -d))
	mkdir $(tmpdir)/submission
	cp -r $(SRC_DIR) $(tmpdir)/submission
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
	-rm -rf $(BNFC_SRC_DIR)
	-mkdir $(BNFC_SRC_DIR)
	touch $(BNFC_SRC_DIR)/.gitkeep

# Clean everything
distclean: vclean
	-rm -f submission.tar.gz

# EOF
