## Line Server Application
- WIP on a Line Server application that serves arbitrary lines of a file to consumers
    via a simple REST endpoint.
- Note: Performance is not production ready for very large files yet. See ideas for potential
    improvements below.

#### How does your system work? (if not addressed in comments in source)
- The REST endpoint defined in com.bjohnson.lineserver.controller.LinesController receives requests
    for a particular index of the file.
- This is passed to the LinesService, which is proxied by Spring due to the @Cacheable annotation.
- If a value for the given index is in the cache, it will be returned immediately, and the implementation
    com.bjohnson.lineserver.service.RandomAccessLinesServiceImpl will not be invoked.
- Otherwise the implementation will run.
- com.bjohnson.lineserver.service.RandomAccessLinesServiceImpl works by preloading the file during start up.
- This yields a list of Integers that correspond to byte positions in the file holding newlines.
- Then, the getLineAtIndex operation uses RandomAccessFile to seek to the end of the previous line from
    the desired index. The desired line is then read and returned.
- The result is then cached for any subsequent request for the same index.
- LinesController outputs the retrieved line with a 200 HTTP status code, or a HTTP 413 status if no line was retrieved.
- To build and run the application you'll need Java 8 available on your PATH.

#### How will your system perform with a 1 GB file? a 10 GB file? a 100 GB file?
- The preloading time is linear with the size of the file and depends on your disk speed.
- Once that is completed, RandomAccessFile is able to seek to the desired line very quickly in response to
    requests.
- On a 2GB file, requests for lines anywhere in the file were coming back in 5ms or less for me.

#### How will your system perform with 100 users? 10000 users? 1000000 users?
- In application.properties I configured the Tomcat max connections to 10000.
- So connections up to 10000 users should be supported without problem.
- For more than that, you could increase the max connections value, but a production implementation for that
    many concurrent users should probably have multiple instances of this application behind a load balancer.
- If we exceed the maximum connections available the request will wait for the next connection to become available,
    resulting in a delay.
- Performance may degrade if IO access to the file by multiple users becomes a bottleneck.
- Another problem may occur if we exceed the maximum open file handles allowed by the operating system.

#### What documentation, websites, papers, etc did you consult in doing this assignment?
- I used https://start.spring.io/ to create a skeleton project to start with.
- I reviewed a few articles such as http://www.baeldung.com/java-read-lines-large-file and
    https://dzone.com/articles/how-to-read-a-big-csv-file-with-java-8-and-stream to find the best approach in Java.
- These approaches didn't provide enough performance so I looked into usage of java.io.RandomAccessFile.

#### What third-party libraries or other tools does the system use? How did you choose each library or framework you used?
- I used Java with Spring Boot, as well as Spring Web and Spring Cache, to start with
    since its what I'm most familiar working with.
- I also used Apache Commons IO just to make it easier to copy the test file for the unit test for LinesService.
- Finally Google Guava includes a thread-safe cache that you can set a maximum size for, which I used as the cache.

#### How long did you spend on this exercise? If you had unlimited more time to spend on this, how would you spend it and how would you prioritize each item?
- I spent approximately 4-6 hours.
- If we had infinite memory or a separate instance (Redis) to hold the cache, we could load each line of the file
    into that cache during preloading and avoid file IO during requests entirely. I assumed we have memory constraints
    that make this unfeasible for a 100 GB file.
- Another approach I considered was be to break down the file into 'chunks' of 10k lines each during startup, and calculate which chunk file
    a requested line lives in based on the provided line number. That would reduce the number of lines we need to read to a max of 10k.
- I considered using a Redis instance to hold the cached lines, though I wasn't sure if that
    would go against the request that I not load the whole file into a database.
- I could have also spent more time finding and testing against sample large text files.
- The REST endpoint is very simple but I could have used Swagger to produce documentation for it.

#### If you were to critique your code, what would you have to say about it?
- I chose a fairly standard approach that someone familiar with Java and Spring would be able
    to understand.
- Separation between the Rest controller and the business logic in the LinesService is done well according to best
    practices.
- There are good unit tests to validate functionality and prevent regressions; although the test file is only 4
    lines, that is enough to check edge cases while ensuring the tests run quickly.
- Because I only have 3 properties I just used an application.properties file, but normally you'd use a YAML
    file to hold configuration.
