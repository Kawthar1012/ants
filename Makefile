JFLAG1 = -cmf
JFLAG2 = -jar
JC = javac
JVM= java 
JAR = jar
F = -Rf
JARFILE = Fourmi.jar
JDOC = javadoc
.SUFFIXES: .java .class

.java.class:
		$(JC) $*.java

CLASSES = \
        Main.java \

MAIN = Main

default: classes

classes: $(CLASSES:.java=.class)

run: $(MAIN).class
		$(JAR) $(JFLAG1)  MANIFEST.MF $(JARFILE)  *.class README.md ./Images/*
		$(RM) *.class
		$(JVM) $(JFLAG2) $(JARFILE)

clean: 
	$(RM) Fourmi.jar ./save/*.fo2 ./save/*.jpg *.class
	$(RM) $(F) ./doc

jdoc:
	$(JDOC) -d ./doc/  *.java