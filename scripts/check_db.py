import mysql.connector

try:
    conn = mysql.connector.connect(
        host="localhost",
        user="annotation",
        password="password",
        database="annotation_db"
    )
    cursor = conn.cursor()
    cursor.execute("SELECT id, text1, text2 FROM text_pair LIMIT 5")
    for row in cursor.fetchall():
        print(row)
except Exception as e:
    print(e)
