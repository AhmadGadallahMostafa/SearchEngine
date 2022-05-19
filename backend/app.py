from cmath import log
from genericpath import exists
from pydoc import doc
from tokenize import Double
from flask import Flask
from flask_pymongo import PyMongo
from nltk.stem import PorterStemmer
from nltk.tokenize import word_tokenize
from flask import request
import operator


app = Flask(__name__)

mongodb_client = PyMongo(app, uri="mongodb://localhost:27017/SearchEngine")

db = mongodb_client.db
invertedIndex = db.get_collection("InvertedIndex")
documentsCollection = db.get_collection("Documents")
ps = PorterStemmer()
scoresMap = {}




@app.route("/")
def home():
    print(db.get_collection("Documents").find_one())
    return "Hello, Flask!"


@app.route("/links", methods=['GET'])
def get_all_users():
    # stem the words in the query
    if request.args.get('q') != None:
        query = request.args.get('q')
        query = query.lower()
        #split the query into words
        query = query.split(" ")
        query = [ps.stem(word) for word in query]
        print(query)
        # get the documents that contain the query words
        for word in query:
            documents = invertedIndex.find({word:{"$exists":True}})
            try:
                document = documents.next()
                docCurrentWord = document.get(word)
                docContainsWordCount = 1
                if docCurrentWord.keys() != None:
                    docContainsWordCount = len(docCurrentWord.keys())
                currWordIDF = log(136 / docContainsWordCount) 
                for docID in docCurrentWord.keys():
                    curDoc = docCurrentWord.get(docID)
                    docWeight = calcWeightTF(curDoc)
                    currDocTF = docWeight / curDoc.get("docLength")
                    currDocScore = currDocTF * currWordIDF
                    if docID in scoresMap:
                        scoresMap[docID] = (scoresMap[docID].real + currDocScore.real)
                    else:
                        scoresMap[docID] = (currDocScore.real)
            except StopIteration:
                print("No documents found")
    sortedScores = sortScoresDescendingly()
    # get the documents from the database
    # get the documents that are in the sortedScores map and return them
    documents = [] 
    for docID in sortedScores.keys():
        documents.append(documentsCollection.find_one({"_id":docID}))
    print(documents)

    return "Hello, Flask!"


def calcWeightTF(doc):
    hFreq = 0
    liFreq = 0
    aFreq = 0
    pFreq = 0
    divFreq = 0
    titleFreq = 0
    if (doc.get('h1') != None):
        hFreq = hFreq + doc.get('h1')
    if (doc.get('h2') != None):
        hFreq = hFreq + doc.get('h2')
    if (doc.get('h3') != None):
        hFreq = hFreq + doc.get('h3')
    if (doc.get('h4') != None):
        hFreq = hFreq + doc.get('h4')
    if (doc.get('h5') != None):
        hFreq = hFreq + doc.get('h5')
    if (doc.get('h6') != None):
        hFreq = hFreq + doc.get('h6')
    if (doc.get('li') != None):
        liFreq = doc.get('li')
    if (doc.get('a') != None):
        aFreq = doc.get('a')
    if (doc.get('p') != None):  
        pFreq = doc.get('p')
    if (doc.get('div') != None):
        divFreq = doc.get('div')
    if (doc.get('title') != None):
        titleFreq = doc.get('title')
    hFreq = hFreq * 2
    return (hFreq + liFreq + aFreq + pFreq + divFreq + titleFreq * 3)


def sortScoresDescendingly():
    # sort accorindg to the value of the key
    sortedScores = scoresMap.items()
    sortedScores = sorted(sortedScores, key=operator.itemgetter(1), reverse=True)
    # convert list of to map
    sortedScores = dict(sortedScores)
    print(sortedScores)
    return sortedScores


