import csv

with open('data/dataset_similarite.csv', 'r') as f:
    reader = csv.reader(f)
    next(reader)
    for row in reader:
        print(f"UPDATE text_pair SET text1='{row[1].replace(chr(39), chr(39)+chr(39))}', text2='{row[2].replace(chr(39), chr(39)+chr(39))}' WHERE id='{row[0]}' AND dataset_id=2;")

with open('data/dataset_sentiment.csv', 'r') as f:
    reader = csv.reader(f)
    next(reader)
    for row in reader:
        print(f"UPDATE text_pair SET text1='{row[1].replace(chr(39), chr(39)+chr(39))}', text2=NULL WHERE id='{row[0]}' AND dataset_id=1;")

with open('data/dataset_nli.csv', 'r') as f:
    reader = csv.reader(f)
    # The first row of NLI has an empty line? Wait, I saw earlier head dataset_nli.csv had an empty first line.
    # Let me check...
