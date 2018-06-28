## Line Server Application
- A Line Server application that serves arbitrary lines of a file to consumers
    via a simple REST endpoint.
- The objective is to offer fastest possible performance, even for very large files.
    To achieve that we currently use a preloading approach that impacts start up time.

#### How does your system work? (if not addressed in comments in source)
- The REST endpoint defined in com.bjohnson.lineserver.controller.LinesController receives requests
    for a particular index of the file.
- This is passed to the LinesService, which is proxied by Spring due to the @Cacheable annotation.
- If a value for the given index is in the cache, it will be returned immediately, and the implementation
    com.bjohnson.lineserver.service.RandomAccessLinesServiceImpl will not be invoked.
- Otherwise the implementation will run.
- com.bjohnson.lineserver.service.RandomAccessLinesServiceImpl works by preloading the file during start up.
    For a 2GB file this takes slightly over a minute for me.
- The preloading process yields a list of longs that correspond to byte positions in the file holding newline chars.
- Then, the getLineAtIndex() operation uses RandomAccessFile to seek to the end of the previous line from
    the desired index. The desired line is then read and returned.
- The result is then cached for any subsequent request for the same index.
- LinesController outputs the retrieved line with a 200 HTTP status code, or a HTTP 413 status if no line was retrieved.
- To build and run the application you'll need Java 8 available on your PATH.

#### How will your system perform with a 1 GB file? a 10 GB file? a 100 GB file?
- The preloading time is linear with the size of the file and depends on your disk speed.
- Once that is completed, RandomAccessFile is able to seek to the desired line very quickly in response to
    requests.
- On a 2GB file, requests for lines anywhere in the file come back in less than 50ms using simple curl requests.
- The RandomAccessFile.seek(pos) operation seems to perform the same regardless of the desired position in a
    2GB file.

#### How will your system perform with 100 users? 10000 users? 1000000 users?
- In application.properties I configured the Tomcat max connections to 10000.
- So connections up to 10000 users should be supported without problem.
- For more than that, you could increase the max connections value, but a production implementation for that
    many concurrent users should probably have multiple instances of this application behind a load balancer.
- If we exceed the maximum connections available, the request will wait for the next connection to become available,
    resulting in a delay.
- Performance may degrade if IO access to the file by multiple users becomes a bottleneck.
- Another problem may occur if we exceed the maximum open file handles allowed by the operating system.

#### What documentation, websites, papers, etc did you consult in doing this assignment?
- I used https://start.spring.io/ to create a skeleton project to start with.
- I reviewed a few articles such as http://www.baeldung.com/java-read-lines-large-file and
    https://dzone.com/articles/how-to-read-a-big-csv-file-with-java-8-and-stream to find the best approach in Java.
- These approaches did not provide enough performance so I looked into usage of java.io.RandomAccessFile.

#### What third-party libraries or other tools does the system use? How did you choose each library or framework you used?
- I used Java with Spring Boot, as well as Spring Web and Spring Cache, to start with
    since its what I'm most familiar working with.
- I also used Apache Commons IO just to make it easier to copy the test file for the unit test for LinesService.
- Finally Google Guava includes a thread-safe cache that you can set a maximum size for, which I used as the cache.

#### How long did you spend on this exercise? If you had unlimited more time to spend on this, how would you spend it and how would you prioritize each item?
- I spent approximately 4-8 hours.
- If we had infinite memory or a separate server instance (Redis) to hold the cache, we could load each line of the file
    into that cache during preloading and avoid file IO during requests entirely. I assumed we have memory constraints
    that make this unfeasible for a 100 GB file.
- Once we have the list of line ending positions, RandomAccessFile seek operations work very quickly.
- However, the cost of retrieving the line ending positions delays start up.
- One way to avoid that would be to rework the preloading to act in the background rather than block
    application start up. That would introduce the trade off that the full file content wouldn't be available
    until it finished.
- I also could have used JMeter to test the application scalability under high concurrency,
    and resolve any issues that turned up.
- The data structure for holding the line ending positions is just an ArrayList, for a cluster
    I'd consider using a distributed data structure via Hazelcast so we only load
    that once.      

#### If you were to critique your code, what would you have to say about it?
- I chose a fairly standard approach that someone familiar with Java and Spring would be able
    to understand.
- Separation between the Rest controller and the business logic in the LinesService is done well according to best
    practices.
- There are good unit tests to validate functionality and prevent regressions; although the test file is only 4
    lines, that is enough to check edge cases while ensuring the tests run quickly.
    I was able to use existing tests from a simpler approach to validate the RandomAccessFile based approach.
- Because I only have 3 properties I just used an application.properties file, but normally you'd use a YAML
    file to hold configuration.
