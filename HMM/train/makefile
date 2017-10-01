all: hey

hey: predict.o hmm.o main.o
	g++ predict.o hmm.o main.o -o hey

main.o: main.cpp
	g++ -c main.cpp

predict.o: predict.cpp predict.h
	g++ -c predict.cpp predict.h

hmm.o: hmm.cpp hmm.h
	g++ -c hmm.cpp hmm.h

clean:
	rm -rf *.o hey
