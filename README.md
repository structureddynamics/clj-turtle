# clj-turtle

`clj-turtle` is a really simple Clojure DSL to produce compatible `RDF/Turtle` data. 

The Turtle code that is produced by this DSL is currently quite verbose. This means that all the URIs are extended, that the triple quotes are used and that the triples are fully described.

The goal of this DSL is to be able to easily generate RDF data from Clojure code. One good example where `cjl-turtle` is used is when RDF data needs to be generated for adding information in a [Open Semantic Framework] [1] (OSF)  instance using the `clj-osf` DSL.

With the six operators described in the `API` section below, we are able to describe any kind of data in RDF by following a few syntactic conventions as defined in the DSL.


## Installation

### Using Linengen

You can easily install `clj-turtle` using Linengen. The only thing you have to do is to add Add `[clj-turtle "0.1.2"]` as a dependency to your `project.clj`.

Then make sure that you downloaded this dependency by running the `lein deps` command.

## API

* `rdf`/`turtle`
	* Generate RDF/Turtle serialized data from a set of triples defined by clj-turtle.
* `defns`
	* Create a new namespace that can be used to create the clj-turtle triples
* `rei`
	* Reify a clj-turtle triple
* `iri`
	* Serialize a URI where you provide the full URI as a string
* `literal` 
	* Serialize a literal value
* `a`
	* Specify the rdf:type of an entity being described

## Usage

### Working with namespaces

The core of this DSL is the `defns` operator. What this operator does is to give you the possibility to create the namespaces you want to use to describe your data. Every time you use a namespace, it will generate a URI reference in the triple(s) that will be serialized in `Turtle`.

However, it is not necessary to create a new namespace every time you want to serialize Trutle data. In some cases, you may not even know what the namespace is since you have the full URI in hands already. It is why there is the `iri` function that let you serialize a full URI without having to use a namespace.

Namespaces are just shorthand versions of full URIs that mean to make your code cleaner an easier to read and maintain.

### Syntatic rules

Here are the general syntactix rules that you have to follow when creating triples in a `(rdf)` or `(turtle)` statement:

1. Wrap all the code using the `(rdf)` or the `(turtle)` operator
2. Every triple need to be explicit. This means that every time you want to create a new triple, you have to mention the `subject`, `predicate` and the `object` of the triple
3. A fourth "reification" element can be added using the `rei` operator
4. The first parameter of any function can be any kind of value: `keywords`, `strings`, `integer`, `double`, etc. They will be properly serialized as strings in Turtle.
 
### Strings and Keywords

As specified in the syntactic rules, at any time, you can use a `string`, a `integer`, a `double` a `keyword` or any other kind of vaue as input of the defined namespaces or the other API calls. You only have to use the way that is more convenient for you or that is the cleanest for your taste.

### More about reification

Note: RDF reification is quite a different concept than Clojure's `reify` macro. So carefully read this section to understand the meaning of the concept in this context.

In RDF, reifying a triple means that we want to add additional information about a specific triple. Let's take this example:

```clojure
  (rdf 
    (foo :bar) (iron :prefLabel) (literal "Bar"))
```

In this example, we have a triple that specify the preferred label of the `:bar` entity. Now, let's say that we want to add "meta" information about that specific triple, like the date when this triple got added to the system for example.

That additional information is considered the "forth" element of a triple. It is defined like this:


```clojure
  (rdf 
    (foo :bar) (iron :prefLabel) (literal "Bar") (rei 
                                                   (foo :date) (literal "2014-10-25" :type :xsd:dateTime)))
```

What we do here is to specify additional information regarding the triple itself. In this case, it is the date when the triple got added into our system.

So, reification statements are really "meta information" about triples. Also not that reification statements doesn't change the semantic of the description of the entities.

## Examples

Now let's see how we can use `clj-turtle` with a series of example. For each example, you will have the `clj-turtle` code and the `RDF/Turtle` code that is generated from that code.

### Create a new namespace

The first thing we have to do is define the namespaces we will want to use in our code.

```clojure
  (defns iron "http://purl.org/ontology/iron#")
  (defns foo "http://purl.org/ontology/foo#")
  (defns owl "http://www.w3.org/2002/07/owl#")
  (defns rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
  (defns xsd "http://www.w3.org/2001/XMLSchema#")
```

### Create a simple triple

The simplest example is to create a simple triple. What this triple does is to define the preferred label of a `:bar` entity:

```clojure
  (rdf 
    (foo :bar) (iron :prefLabel) (literal "Bar"))
```

Output:

```
<http://purl.org/ontology/foo#bar> <http://purl.org/ontology/iron#prefLabel> """Bar""" .
```

### Create a series of triples

This example shows how we can describe more than one attribute for our `bar` entity:

```clojure
  (rdf 
    (foo :bar) (a) (owl :Thing)
    (foo :bar) (iron :prefLabel) (literal "Bar")
    (foo :bar) (iron :altLabel) (literal "Foo"))
```

Output:

```
  <http://purl.org/ontology/foo#bar> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>   <http://www.w3.org/2002/07/owl#Thing>  .
  <http://purl.org/ontology/foo#bar> <http://purl.org/ontology/iron#prefLabel> """Bar""" .
  <http://purl.org/ontology/foo#bar> <http://purl.org/ontology/iron#altLabel> """Foo""" .
```

Note: we prefer having one triple per line. However, it is possible to have all the triples in one line, but this will produce less readable code:

### Just use keywords

It is possible to use `keywords` everywhere, even in `(literals)`

```clojure
  (rdf 
    (foo :bar) (a) (owl :Thing)
    (foo :bar) (iron :prefLabel) (literal :Bar)
    (foo :bar) (iron :altLabel) (literal :Foo))
```

Output:

```
<http://purl.org/ontology/foo#bar> <http://www.w3.org/1999/02/22-rdf-syntax-ns#> <http://www.w3.org/2002/07/owl#Thing>  .
<http://purl.org/ontology/foo#bar> <http://purl.org/ontology/iron#prefLabel> """:Bar""" .
<http://purl.org/ontology/foo#bar> <http://purl.org/ontology/iron#altLabel> """:Foo""" .
```

### Just use strings

It is possible to use `strings` everywhere, even in `namespaces`:

```clojure
  (rdf 
    (foo "bar") (a) (owl "Thing")
    (foo "bar") (iron :prefLabel) (literal "Bar")
    (foo "bar") (iron :altLabel) (literal "Foo"))
```

Output:

```
<http://purl.org/ontology/foo#bar> <http://www.w3.org/1999/02/22-rdf-syntax-ns#> <http://www.w3.org/2002/07/owl#Thing>  .
<http://purl.org/ontology/foo#bar> <http://purl.org/ontology/iron#prefLabel> """Bar""" .
<http://purl.org/ontology/foo#bar> <http://purl.org/ontology/iron#altLabel> """Foo""" .
```

### Specifying a datatype in a literal

It is possible to specify a `datatype` for every `(literal)` you are defining. You only have to use the `:type` option and to specify a `XSD` datatype as value:

```clojure
  (rdf 
    (foo "bar") (foo :probability) (literal 0.03 :type :xsd:double))
```

Equivalent code are:

```clojure
  (rdf 
    (foo "bar") (foo :probability) (literal 0.03 :type (xsd :double)))
```

```clojure
  (rdf 
    (foo "bar") (foo :probability) (literal 0.03 :type (iri "http://www.w3.org/2001/XMLSchema#double")))
```

Output:

```
<http://purl.org/ontology/foo#bar> <http://purl.org/ontology/foo#probability> """0.03"""^^xsd:double .
```

### Specifying a language for a literal

It is possible to specify a language string using the `:lang` option. The language tag should be a compatible ISO 639-1 language tag.

```clojure
  (rdf 
    (foo "bar") (iron :prefLabel) (literal "Robert" :lang :fr))
```

Output:

```
<http://purl.org/ontology/foo#bar> <http://purl.org/ontology/iron#prefLabel> """Robert"""@fr .
```

### Defining a type using the `a` operator

It is possible to use the `(a)` predicate as a shortcut to define the `rdf:type` of an entity:

```clojure
  (rdf 
    (foo "bar") (a) (owl "Thing"))
```

Output:

```
<http://purl.org/ontology/foo#bar> <http://www.w3.org/1999/02/22-rdf-syntax-ns#> <http://www.w3.org/2002/07/owl#Thing>  .
```

This is a shortcut for:

```clojure
  (rdf 
    (foo "bar") (rdf :type) (owl "Thing"))
```


### Specifying a full URI using the `iri` operator

It is possible to define a `subject`, a `predicate` or an `object` using the `(iri)` operator if you want to defined them using the full URI of the entity:

```clojure
  (rdf 
    (iri "http://purl.org/ontology/foo#bar") (iri "http://www.w3.org/1999/02/22-rdf-syntax-ns#type) (iri http://www.w3.org/2002/07/owl#Type))
```

Output:

```
<http://purl.org/ontology/foo#bar> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Type>  .
```


### Simple reification

It is possible to reify any triple suing the `(rei)` operator as the forth element of a triple:

```clojure
  (rdf 
    (foo :bar) (iron :prefLabel) (literal "Bar") (rei 
                                                   (foo :date) (literal "2014-10-25" :type :xsd:dateTime)))
```

Output:

```
<http://purl.org/ontology/foo#bar> <http://purl.org/ontology/iron#prefLabel> """Bar""" .
<rei:6930a1f93513367e174886cb7f7f74b7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> .
<rei:6930a1f93513367e174886cb7f7f74b7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://purl.org/ontology/foo#bar>  .
<rei:6930a1f93513367e174886cb7f7f74b7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://purl.org/ontology/iron#prefLabel>  .
<rei:6930a1f93513367e174886cb7f7f74b7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> """Bar""" .
<rei:6930a1f93513367e174886cb7f7f74b7> <http://purl.org/ontology/foo#date> """2014-10-25"""^^xsd:dateTime .
```

### Reify multiple properties

It is possible to add multiple reification statements:

```clojure
  (rdf 
    (foo :bar) (iron :prefLabel) (literal "Bar") (rei 
                                                   (foo :date) (literal "2014-10-25" :type :xsd:dateTime)
                                                   (foo :bar) (literal 0.37)))
```

Output:

```
<http://purl.org/ontology/foo#bar> <http://purl.org/ontology/iron#prefLabel> """Bar""" .
<rei:6930a1f93513367e174886cb7f7f74b7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> .
<rei:6930a1f93513367e174886cb7f7f74b7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://purl.org/ontology/foo#bar>  .
<rei:6930a1f93513367e174886cb7f7f74b7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://purl.org/ontology/iron#prefLabel>  .
<rei:6930a1f93513367e174886cb7f7f74b7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> """Bar""" .
<rei:6930a1f93513367e174886cb7f7f74b7> <http://purl.org/ontology/foo#date> """2014-10-25"""^^xsd:dateTime .
<rei:6930a1f93513367e174886cb7f7f74b7> <http://purl.org/ontology/foo#bar> """0.37""" .
```

### Using clj-turtle with clj-osf

`clj-turtle` is meant to be used in Clojure code to simplify the creation of RDF data. Here is an example of how `clj-turtle` can be used to generate RDF data to feed to the [OSF Crud: Create] [2] web service endpoint via the `clj-osf` DSL:

```clojure
[require '[clj-osf.crud :as crud])

(crud/create
  (crud/dataset "http://test.com/datasets/foo")
  (crud/document
    (rdf
     (iri link) (a) (bibo :Article)
     (iri link) (iron :prefLabel) (literal "Some article")))
  (crud/is-rdf-n3)
  (crud/full-indexation-mode))
```

### Using the `turtle` alias operator

Depending on your taste, it is possible to use the `(turtle)` operator instead of the `(rdf)` one to generate the RDF/Turtle code:

```clojure
  (turtle 
    (foo "bar") (iron :prefLabel) (literal "Robert" :lang :fr))
```

Output:

```
<http://purl.org/ontology/foo#bar> <http://purl.org/ontology/iron#prefLabel> """Robert"""@fr .
```

### Merging `clj-turtle` 

Depending the work you have to do in your Clojure application, you may have to generate the Turtle data using a more complex flow of operations. However, this is not an issue for `clj-turtle` since the only thing you have to do is to concatenate the triples you are creating. You can do so using a simple call to the `str` function, or you can create more complex processing using loopings, mappins, etc that endup with a `(apply str)` to generate the final Turtle string.

```clojure
  (str
    (rdf 
      (foo "bar") (a) (owl "Thing"))
    (rdf
      (foo "bar") (iron :prefLabel) (literal "Bar")
      (foo "bar") (iron :altLabel) (literal "Foo")))
```

Output:

```
<http://purl.org/ontology/foo#bar> <http://www.w3.org/1999/02/22-rdf-syntax-ns#> <http://www.w3.org/2002/07/owl#Thing>  .
<http://purl.org/ontology/foo#bar> <http://purl.org/ontology/iron#prefLabel> """Bar""" .
<http://purl.org/ontology/foo#bar> <http://purl.org/ontology/iron#altLabel> """Foo""" .
```




## License

Copyright © 2014 Structured Dynamics LLC.

Distributed under the Eclipse Public License either version 1.0

[1]:http://opensemanticframework.org
[2]:http://wiki.opensemanticframework.org/index.php/CRUD:_Create