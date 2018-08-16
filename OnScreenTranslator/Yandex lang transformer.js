var arr = [{id:'af', name:'Afrikaans'},
{id:'am', name:'Amharic'},
{id:'ar', name:'Arabic'},
{id:'az', name:'Azerbaijani'},
{id:'ba', name:'Bashkir'},
{id:'be', name:'Belarusian'},
{id:'bg', name:'Bulgarian'},
{id:'bn', name:'Bengali'},
{id:'bs', name:'Bosnian'},
{id:'ca', name:'Catalan'},
{id:'ceb', name:'Cebuano'},
{id:'cs', name:'Czech'},
{id:'cy', name:'Welsh'},
{id:'da', name:'Danish'},
{id:'de', name:'German'},
{id:'el', name:'Greek'},
{id:'en', name:'English'},
{id:'eo', name:'Esperanto'},
{id:'es', name:'Spanish'},
{id:'et', name:'Estonian'},
{id:'eu', name:'Basque'},
{id:'fa', name:'Persian'},
{id:'fi', name:'Finnish'},
{id:'fr', name:'French'},
{id:'ga', name:'Irish'},
{id:'gd', name:'Scottish Gaelic'},
{id:'gl', name:'Galician'},
{id:'gu', name:'Gujarati'},
{id:'he', name:'Hebrew'},
{id:'hi', name:'Hindi'},
{id:'hr', name:'Croatian'},
{id:'ht', name:'Haitian'},
{id:'hu', name:'Hungarian'},
{id:'hy', name:'Armenian'},
{id:'id', name:'Indonesian'},
{id:'is', name:'Icelandic'},
{id:'it', name:'Italian'},
{id:'ja', name:'Japanese'},
{id:'jv', name:'Javanese'},
{id:'ka', name:'Georgian'},
{id:'kk', name:'Kazakh'},
{id:'km', name:'Khmer'},
{id:'kn', name:'Kannada'},
{id:'ko', name:'Korean'},
{id:'ky', name:'Kyrgyz'},
{id:'la', name:'Latin'},
{id:'lb', name:'Luxembourgish'},
{id:'lo', name:'Lao'},
{id:'lt', name:'Lithuanian'},
{id:'lv', name:'Latvian'},
{id:'mg', name:'Malagasy'},
{id:'mhr', name:'Mari'},
{id:'mi', name:'Maori'},
{id:'mk', name:'Macedonian'},
{id:'ml', name:'Malayalam'},
{id:'mn', name:'Mongolian'},
{id:'mr', name:'Marathi'},
{id:'mrj', name:'Hill Mari'},
{id:'ms', name:'Malay'},
{id:'mt', name:'Maltese'},
{id:'my', name:'Burmese'},
{id:'ne', name:'Nepali'},
{id:'nl', name:'Dutch'},
{id:'no', name:'Norwegian'},
{id:'pa', name:'Punjabi'},
{id:'pap', name:'Papiamento'},
{id:'pl', name:'Polish'},
{id:'pt', name:'Portuguese'},
{id:'ro', name:'Romanian'},
{id:'ru', name:'Russian'},
{id:'si', name:'Sinhalese'},
{id:'sk', name:'Slovak'},
{id:'sl', name:'Slovenian'},
{id:'sq', name:'Albanian'},
{id:'sr', name:'Serbian'},
{id:'su', name:'Sundanese'},
{id:'sv', name:'Swedish'},
{id:'sw', name:'Swahili'},
{id:'ta', name:'Tamil'},
{id:'te', name:'Telugu'},
{id:'tg', name:'Tajik'},
{id:'th', name:'Thai'},
{id:'tl', name:'Tagalog'},
{id:'tr', name:'Turkish'},
{id:'tt', name:'Tatar'},
{id:'udm', name:'Udmurt'},
{id:'uk', name:'Ukrainian'},
{id:'ur', name:'Urdu'},
{id:'uz', name:'Uzbek'},
{id:'vi', name:'Vietnamese'},
{id:'xh', name:'Xhosa'},
{id:'yi', name:'Yiddish'},
{id:'zh', name:'Chinese'}]

function SortByName(a, b){
  var aName = a.name.toLowerCase();
  var bName = b.name.toLowerCase();
  return ((aName < bName) ? -1 : ((aName > bName) ? 1 : 0));
}

arr = arr.sort(SortByName);

var langCodes = ""
var langNames = ""
$(arr).each(function(i,v){
	var code = v.id
  var name = v.name

  langCodes += ("<item>"+code+"</item><!-- "+name+" -->")
    /* console.log(langCodes) */

    langNames += ("<item>"+name+"</item>")
    /* console.log(langNames) */
})

console.log(langCodes)
console.log(langNames)
