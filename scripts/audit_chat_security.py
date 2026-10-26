import re
import html

with open('/Users/madarauchiha/connecto/website/static/index.html') as f:
    text = f.read()

lines = [line.strip() for line in text.split('\n') if 'onclick=' in line and '${' in line]
print(f'Matching lines: {len(lines)}')
for i, l in enumerate(lines[:50]):
    print(f'{i+1}: {l}')
