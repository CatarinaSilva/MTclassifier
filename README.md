# MTclassifier

Machine translation classifier - a classifier that learns to distinguish between Machine translation (MT) or human translation.

## Intro

This is a Java Project created for a challenge proposed by Unbabel. The challenge was the following:

* Goal - to detect if a text is written by a human or if it is a direct result of MT
* Resources given - a training set composed by an integer {0,1} and a sentence
  * 0 means MT
  * 1 means human 
* A test set for final evaluation

The projects were evaluated by performance in a test set, and this project was awarded 2nd place in the challenge. 
Both given sets were in Spanish.

## Method implemented

I implemented a simple Bayes probabilistic model with Bag of Words, based on the figure expressions (in red are the final ones implemented) where C , c -> class	;     D , d -> observation ;    x_i , w_i -> word i ;

![Bayes expressions](https://github.com/CatarinaSilva/MTclassifier/blob/master/expressoes.png)


Instead of using simply single words, to include some context with simplicity, I used also duples and tripes, this is sets pf two and three words, respectively. For each type of "words" (single, duples, triples) a probability P is calculated with the given expressions. Then this probability was multiplied by a weight (Ps, Pd, Pt, respectively), which was tuned with a method explained later on. The final probability is the sum of all these.

In case the sentence includes numbers, a tag was used (#NUM#) since the same expression could be repeated with different numbers and is, in terms of language, similar. The numbers are not counted as single words, and only the tag s used in duples and triples.

	example(for Spanish):
  (...) los 320 individuos de actuación (...)   -->   (...) los #NUM# individuos de actuación (...) 
"Words" : [los/individuos/de/actuación] ; [los #NUM#/#NUM# individuos/individuos de/de actuación] ; [los #NUM# individuos/#NUM# individuos de/individuos de actuación]

The Java code has a training routine, a test routine, and 
A implementação foi totalmente feita em Java, com uma rotina de treino (that substitutes _  by 0 or 1) and a routine that uses a training set parted as train/test for weight tuning. In training, three dictionaries are created for each class - 0 (Machine translation) e 1 (human) - where  the key is the "word" and the value is the number of counts in the training set. In test a weighted meean is used with the above mention expressions.

For weight tuning the process was the following
* Remove one instance of a sentence and train with the remaining
* Test the sentence and save the result - success (1) or fail (0)
* Repeat until 1000 iterations
* Return the error percentage

I obtained the following table:


|      Weights [ Ps Pd Pt ]     |   Error (%)  | 
| ----------------------------- | ------------ |
|           [1,1,1]             |        3.0   |    
|           [1,1,0]             |        3.6   |   
|           [1,0,0]             |       24.2   |  
|           [1,0,1]             |        1.3   |   
|           [0,0,1]             |        2.1   |   
|           [0,1,1]             |        1.4   |  
|           [0,1,0]             |        4.0   |     
|           [1,2,2]             |        1.2   |    
|           [1,2,3]             |        0.9   |      
|           [1,3,3]             |        1.3   |   


And so I used in the program [1,2,3]
