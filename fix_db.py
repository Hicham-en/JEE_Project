import csv

def quote(s):
    return s.replace("'", "''")

sql = []

with open('data/dataset_sentiment.csv', 'r') as f:
    reader = csv.reader(f)
    next(reader) # skip header
    for i, row in enumerate(reader):
        sql.append(f"UPDATE text_pair SET text1='{quote(row[1])}', text2=NULL WHERE id={row[0]};")

with open('data/dataset_similarite.csv', 'r') as f:
    reader = csv.reader(f)
    next(reader) 
    for i, row in enumerate(reader):
        sql.append(f"UPDATE text_pair SET text1='{quote(row[1])}', text2='{quote(row[2])}' WHERE id={int(row[0]) + 10};")

with open('data/dataset_nli.csv', 'r') as f:
    reader = list(csv.reader(f))
    for row in reader:
        if not row or not row[0].isdigit():
            continue
        sql.append(f"UPDATE text_pair SET text1='{quote(row[1])}', text2='{quote(row[2])}' WHERE id={int(row[0]) + 20};")

with open('update.sql', 'w') as f:
    f.write("\n".join(sql))
