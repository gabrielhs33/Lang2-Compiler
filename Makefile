#######################################################################
### lang2-compiler                                                  ###
### Autores:                                                        ###
###  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ###
###  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ###
#######################################################################

all: dist

Lang2Compiler.class: lang2/parser/Lang2Lexer.java lang2/parser/Lang2Parser.java
	javac -cp .:tools/java-cup-11b-runtime.jar Lang2Compiler.java

dist: Lang2Compiler.class
	# Empacota classes compiladas em um JAR distribuível
	find . -name "*.class" > .classlist
	jar cf lang2-compiler.jar @.classlist
	rm .classlist

lang2/parser/Lang2Parser.java:
	java -jar tools/java-cup-11b.jar -destdir lang2/parser/ lang2/parser/lang2.cup
lang2/parser/Lang2Lexer.java:
	java -jar tools/jflex.jar -nobak -d lang2/parser lang2/parser/lang2.flex

cleanClasses:
	find -name "*.class" -delete

clean: cleanClasses cleanParser

cleanParser:
	rm -f lang2/parser/Lang2Lexer.java
	rm -f lang2/parser/Lang2Parser.java
	rm -f lang2/parser/Lang2ParserSym.java

cleanSamples:
	find -name "*.dot" -delete
	find -name "*.jpeg" -delete
