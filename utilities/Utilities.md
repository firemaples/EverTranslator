** Run the following script to export the supported language of Google ML Kit to Android resource format in console.

```
const codeMode = true;

let resText = '';

document.querySelectorAll('table').forEach(function(table, index){
    if (index > 0) resText += '\n\n';

    let tableName = '';
    switch (index) {
        case 0:
            tableName = 'Supported languages';
            break;
        case 1:
            tableName = 'Experimental languages';
            break;
        case 2:
            tableName = 'Mapped languages';
            break;
        default:
            break;
    }
    resText += `<!-- table (${tableName}) -->`;

    Array.from(table.rows).map((row, index) => {
        if (index == 0) return;

        const cells = Array.from(row.cells);
        const langCode = cells[2].innerText.split(' ')[0];
        const langName = cells[1].innerText;

        if (codeMode)
            resText += `\n<item>${langCode}</item> <!-- ${langName} -->`;
        else
            resText += `\n<item>${langName}</item> <!-- ${langCode} -->`;
    })
})

console.log(resText);
```
