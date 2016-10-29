[![Status](https://img.shields.io/travis/EmilyBjoerk/gonector.svg)](https://travis-ci.org/EmilyBjoerk/gonector)
[![Code Coverage](https://img.shields.io/codecov/c/github/EmilyBjoerk/gonector/master.svg)](https://codecov.io/github/EmilyBjoerk/gonector?branch=master)
[![Bintray](https://img.shields.io/bintray/v/emilybjoerk/lisoft/gonector.svg)](https://bintray.com/emilybjoerk/lisoft/gonector)

# GoNector
GoNector is an easy to use framework implementing the Go Text Protocol (GTP) version 2 for Java. 

# Downloading
You can use gonector in your gradle or maven projects as follows:

Gradle:
```gradle
compile 'org.li-soft.gonector:gonector:1.0.0'
```

Maven:
```xml
<dependency>
  <groupId>org.li-soft.gonector</groupId>
  <artifactId>gonector</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```


# JavaDoc
The JavaDoc can be found here: https://emilybjoerk.github.io/gonector/javadoc/ 

# Usage
The easiest way to use GoNector is to connect it to standard input and standard output like so:

```java
    public static void main(String[] args) {
        // Create an instance of your go engine
        GoEngine engine = new MyGoEngine();
        
        // Create reader and writer for standard input and output
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Writer writer = new BufferedWriter(new OutputStreamWriter(System.out));

        // Run the protocol parsing loop
        GoTextProtocol gtp = new GoTextProtocol(reader, writer, engine);
        gtp.call();
    }
```

All that is left for you to do is to implement the `GoEngine` interface to implement your bot. Documentation can be found in the JavaDoc, also for reference you might want to read the Go Text Protocol (GTP) specification here: https://www.lysator.liu.se/~gunnar/gtp/.

Once you have implemented your `GoEngine` and added the above to your main method you should be able to play against the bot using any software that supports the GTP version 2. Like for example: https://sourceforge.net/projects/gogui/. Simply build your project to a fat jar and tell GoGui or whatever program you choose to use to execute `java -jar /path/to/thejarfile.jar`.

Here is an example bot that plays random moves:

```java
class RandomGoEngine implements GoEngine {
    private int size;
    private final Random rng = new Random();
    private Player[] board;

    @Override public String getName() {
        return "Random Engine";
    }

    @Override public String getVersion() {
        return "0.0.1";
    }

    @Override public boolean resizeBoard(int aSize) {
        size = aSize;
        return true;
    }

    @Override public void newGame() {
        board = new Player[size*size];
    }

    @Override public void setKomi(float komi) {
        // This bot doesn't care about komi. 
    }

    @Override public boolean addMove(Move aMove, Player aPlayer) {
        if(aMove == Move.PASS || aMove == Move.RESIGN)
            return true;

        int i = aMove.x + aMove.y*size;

        if(null != board[i])
            return false;
        board[i] = aPlayer;

        // we'll ignore ko, hopefully we won't randomly play a ko-fight...
        return true;
    }

    @Override public Move nextMove(Player player) {
        Move c;
        boolean success;
        int triesLeft = 3;

        do{
            c = new Move(rng.nextInt(size), rng.nextInt(size));
            success = addMove(c, player);
            triesLeft--;
        }while (!success && triesLeft >= 0);

        if(!success)
            return Move.RESIGN;
        return c;
    }
}
```
