make:
	javac *.java

run:
	java COS341_p1 data.txt
	
clean:
	-@rm *.class