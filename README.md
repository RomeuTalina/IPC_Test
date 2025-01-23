A simple IPC Protocol for sending text messages through processes, written using Java 17.

Functionality:
- Before sending a message, checks if there is enough space, as strings have variable length.
- Every time a message is sent, the length is specified before it, so the consumer knows how many bytes to read.
- Put and Get indexes are stored withing the buffer.
