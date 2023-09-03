# CONTRIBUTED PROVIDERS

## Link providers

A link provider is an EDN file with an `:fn` key that maps to a function.
The function takes zero arguments, and returns a sequence of maps with a `:url` key (required), and optionally a `:title` key.

Here is an example provider:

``` clojure
{:fn (fn []
       [{:title "Simple Made easy"
         :url "https://www.youtube.com/watch?v=SxdOUGdseq4"}
        {:title "Are you serious?"
         :url "https://visakanv.substack.com/p/are-you-serious"}])}
```

## Adding more providers?

For now, I'd love to add as many providers as possible.

1. Linking to your own content is perfectly fine, and encouraged.
2. Linking to other people's content is also perfectly fine, and encouraged.
3. Don't write code that can cause harm to the user's system.
   In the `fn` block:
   - Don't read files that you shouldn't read
   - Don't send data off in HTTP requests
   - Don't dynamically evaluate code you get over the wire.

As the author of this project, I (Teodor) am responsible for avoiding harm made to the consumers of this library.
