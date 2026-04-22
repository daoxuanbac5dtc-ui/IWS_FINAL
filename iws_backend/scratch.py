import re

with open(r'c:\Users\MSI\Downloads\IWS_FINAL\Duantotnghiep-main\Duantotnghiep-main\DataDuAnTotNgiepCuoi.sql', 'r', encoding='utf-8') as f:
    sql = f.read()

# Fix BIT defaults
sql = sql.replace('BIT DEFAULT 0', 'TINYINT(1) DEFAULT 0')
sql = sql.replace('BIT', 'TINYINT(1)')

# Remove USE master
sql = re.sub(r'(?i)use\s+master\b\s*go', '', sql)
sql = re.sub(r'(?i)use\s+iws_db\b\s*go', '', sql)

# CREATE DATABASE string
sql = sql.replace('CREATE DATABASE PRO2113', 'CREATE DATABASE IF NOT EXISTS PRO2113 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci')

# Replace IDENTITY(1,1) -> AUTO_INCREMENT
sql = re.sub(r'(?i)\bIDENTITY\(\s*1\s*,\s*1\s*\)', 'AUTO_INCREMENT', sql)

# Replace GETDATE() -> NOW()
sql = re.sub(r'(?i)\bGETDATE\(\)', 'NOW()', sql)

# Replace NVARCHAR -> VARCHAR
sql = re.sub(r'(?i)\bNVARCHAR\b', 'VARCHAR', sql)

# Replace N' -> ' (case-sensitive)
sql = sql.replace("N'", "'")

# Fix missing ADD in constraints.
sql = re.sub(r',\s*CONSTRAINT\s', ', ADD CONSTRAINT ', sql)

# Fix missing semicolons on UPDATE statements.
sql = re.sub(r'(?mi)^(UPDATE[^\n;]+?)(;)?(\s*--.*)?$', r'\1;\3', sql)

# GO -> ;
sql = re.sub(r'(?im)^\s*GO\s*$', ';', sql)

# Wrap with FOREIGN_KEY_CHECKS
sql = "SET FOREIGN_KEY_CHECKS = 0;\n" + sql + "\nSET FOREIGN_KEY_CHECKS = 1;\n"

with open(r'c:\Users\MSI\Downloads\IWS_FINAL\Duantotnghiep-main\Duantotnghiep-main\DataDuAnTotNgiepCuoi_MySQL.sql', 'w', encoding='utf-8') as f:
    f.write(sql)
print('Hoan thanh tao file SQL')
