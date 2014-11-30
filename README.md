# Presentation

BookMixer is a small java project to manipulate text, based on the Stanford NLP library.
Its main usage is to mix books, i.e :
- pick the story from a first book
- pick names and locations in a second book
- replaces the story's names and locations with the second book's names and locations

It leverages the NER package of the StanforNLP Library, trained on an english language corpus.
It was developed and used at the end of NaNoGenMo204.

# Usage

First, build with maven :

```
git clone http://github.com/fredcons/bookmixer
cd bookmixer
mvn clean install
```

Then try the script with your text files :

```
sh ./target/appassembler/bin/BookMixer --names /path/to/names/file--story /path/to/storyfile --output /path/to/results [--randomlocations] [--randompeople] [--keeplocations] [--keeppeople]
```

The script tries to match the people and locations with their respective frequencies (meaning that the most frequent character in the story will be replaced by the most frequent character in the names file);
You can change this behaviour by using --randomlocations and / or --randompeople to pick random names.
I fo you want to swap characters only, you  can use --keeplocations (or --keeppeople to swap locations only)

The script will generate in the target folder the following files :

```
~ tree /path/to/results
/path/to/results
├── annotated_names.txt    # an annotated version of the names file
├── annotated_story.txt    # an annotated version of the story file
├── existing_locations.txt # locations found in story file
├── existing_people.txt    # characters found in story file
├── new_locations.txt      # locations found in names file
├── new_people.txt         # characters found in namesfile
└── resulting_story.txt    # the result : the story file, with the the names file's characters and locations
```

These files will give you some insights about the process, notably names and locations statistics.

You can also use a script to simply extract names and locations count :

```
sh ./target/appassembler/bin/NamesExtractor --file /path/to/your/file
```

Or view the raw annotated file :

```
sh ./target/appassembler/bin/TextAnnotator --file /path/to/your/file
```



