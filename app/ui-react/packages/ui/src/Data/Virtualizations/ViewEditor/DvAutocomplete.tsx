import * as CodeMirror from 'codemirror';

export const dvLanguageMode = 'text/x-teiidddl';

const keywords =
  'access add after all allocate alter analyze and any are array array_agg arraytable as asc asensitive asymetric atomic authenticated authorization auto_increment avg ' +
  'begin between bigdecimal bigint biginteger binary blob both break by byte call called cascaded case cast chain char character check clob close collate column columns commit condition connect constraint content ' +
  'continue control convert corresponding count count_big create criteria cross cume_dist current current_date current_time current_timestamp current_user cursor cycle data database datalink date day deallocate dec ' +
  'decimal declare default delete delimiter dense_rank deref desc describe deterministic disabled disconnect distinct dlnewcopy dlpreviouscopy dlurelcompletewrite dlurlcomplete dlurlcompleteonly dlurlpath ' +
  'dlurlpathonly dlurlpathwrite dlurlscheme dlurlsever dlvalue document domain double drop dynamic each element else empty enabled encoding end error escape every except exception excluding exec execute exists explain external ' +
  'false fetch filter first float following for foreign format free from full function geography geometry get global grant group grouping handler has having header hold hour identity if immediate import in ' +
  'including index indicator inner inout input insensitive insert instead int integer intersect interval into is isolation jaas join json jsonarray_agg jsonobject jsontable key language large last lateral leading leave ' +
  'left like like_regex limit listagg local localtime localtimestamp long loop makedep makeind makenotdep mask match max member merge method min minute modifies module month multiset national natural nchar nclob ' +
  'new no nocache none not null nulls numeric object objecttable of offset old on only open option options or order ordinality out outer output over overlaps parameter partition passing path percent_rank position preceding ' +
  'precision prepare preserve primary privileges procedure querystring quote raise range rank reads real recursive references referencing release rename repository result return returning returns revoke right role ' +
  "rollback rollup row row_number rows' savepoint schema scroll search second select selector sensitive sequence serial server session_user set short similar skip smallint some specific specifictype sql sql_tsi_day " +
  'sql_tsi_frac_second sql_tsi_hour sql_tsi_minute sql_tsi_month sql_tsi_quarter sql_tsi_second sql_tsi_week sql_tsi_year sqlexception sqlstate sqlwarning start static stddev_pop stddev_samp string submultilist sum ' +
  'symetric system system_user table temporary text textagg then time timestamp timestampadd timestampdiff timezone_hour timezone_minute to trailing translate translation translator treat trigger trim true type unbounded ' +
  'union unique unknown update upsert usage use user using value values var_pop var_samp varchar variadic varying version view virtual wellformed when whenever where while width window with within without wrapper xml xmlagg ' +
  'xmlattributes xmlbinary xmlcast xmlcomment xmlconcat xmldeclaration xmldocument xmlelement xmlexists xmlforest xmliterate xmlnamespaces xmlparse xmlpi xmlquery xmlserialize xmltable xmltext xmlvalidate yaml year';

const functions =
  'ANY(?param1) ARRAY_AGG(?param1) AVG(?param1) COUNT(?param1) COUNT_BIG(?param1) CUME_DIST() CURRENT_DATE() CURRENT_TIME() CURRENT_TIMESTAMP() DENSE_RANK() EVERY(?param1) FIRST_VALUE(?param1) JSONARRAY_AGG(?param1) ' +
  'LAG(param1,param2,?param3) LAST_VALUE(?param1) LEAD(?param1,?param2,?param3) MAX(?param1) MIN(?param1) NTH_VALUE(?param1,?param2) NTILE(?param1) PERCENT_RANK() RANK() ROW_NUMBER() ROW_NUMBER() SOME(?param1) ' +
  'STDDEV_POP(?param1) STDDEV_SAMP(?param1) STRING_AGG(?param1) ST_GEOMFROMBINARY(?param1) ST_GEOMFROMBINARY(?param1,?param2) SUM(?param1) VAR_POP(?param1) VAR_SAMP(?param1) XMLAGG(?param1) ' +
  'abs(?number) acos(?number) aes_decrypt(?param1,?param2) aes_encrypt(?param1,?param2) array_get(?array,?index) array_length(?array) ascii(?string) asin(?number) atan(?number) atan2(?number1,?number2) ' +
  'bitand(?integer1,?integer2) bitnot(?integer) bitor(?integer1,?integer2) bitxor(?integer1,?integer2) cast(?value,?target) ceiling(?number) char(?code) char_length(?string) character_length(?string) chr(?code) ' +
  'coalesce(?op1,?op2,?op3) commandpayload() commandpayload(?property) concat(?string1,?string2) concat2(?string1,?string2) convert(?value,?target) cos(?number) cot(?number) curdate() current_database() ' +
  'current_time(?param1) current_timestamp(?param1) curtime() dayname(?date) dayofmonth(?date) dayofweek(?date) dayofyear(?date) decodeInteger(?input,?decodeString) decodeInteger(?input,?decodeString,?delimiter) ' +
  'decodeString(?input,?decodeString) decodeString(?input,?decodeString,?delimiter) degrees(?number) endswith(?substring,?string) env(?variablename) env_var(?variablename) exp(?number) floor(?number) ' +
  'formatbigdecimal(?bigdecimal,?format) formatbiginteger(?biginteger,?format) formatdate(?date,?format) formatdouble(?double,?format) formatfloat(?float,?format) formatinteger(?integer,?format) ' +
  'formatlong(?long,?format) formattime(?time,?format) formattimestamp(?timestamp,?format) from_millis(?param1) from_unixtime(?param1) generated_key() generated_key(?param1) hasRole(?roleName) hasRole(?roleType,?roleName) ' +
  'hour(?time) ifnull(?value,?valueIfNull) initcap(?string) insert(?str1,?start,?length,?str2) jsonArray(?param1) jsonParse(?param1,?param2) jsonpathvalue(?param1,?param2) jsonpathvalue(?param1,?param2,?param3) ' +
  'jsonquery(?param1,?param2) jsonquery(?param1,?param2,?param3) jsontoarray(?param1,?param2,?param3,?param4) jsontoxml(?rootElementName,?json) lcase(?string) left(?string,?length) length(?string) ' +
  'locate(?substring,?string) locate(?substring,?string,?index) log(?number) log10(?number) lookup(?codetable,?returnelement,?keyelement,?keyvalue) lower(?string) lpad(?string,?length) lpad(?string,?length,?char) ' +
  'ltrim(?string) md5(?param1) minute(?time) mod(?op1,?op2) modifytimezone(?timestamp,?endTimeZone) modifytimezone(?timestamp,?startTimeZone,?endTimeZone) month(?date) monthname(?date) mvstatus(?param1,?param2) ' +
  'node_id() now() nullif(?op1,?op2) nvl(?value,?valueIfNull) parsebigdecimal(?bigdecimal,?format) parsebiginteger(?biginteger,?format) parsedate(?date,?format) parsedouble(?double,?format) parsefloat(?float,?format) ' +
  'parseinteger(?integer,?format) parselong(?long,?format) parsetime(?time,?format) parsetimestamp(?timestamp,?format) pi() power(?base,?power) quarter(?date) radians(?number) rand() rand(?seed) ' +
  'regexp_replace(?param1,?param2,?param3) regexp_replace(?param1,?param2,?param3,?param4) repeat(?string,?count) replace(?string,?substring,?replacement) right(?string,?length) round(?number,?places) ' +
  'rpad(?string,?length) rpad(?string,?length,?char) rtrim(?string) second(?time) session_id() sha1(?param1) sha2_256(?param1) sha2_512(?param1) sign(?number) sin(?number) space(?count) sqrt(?number) st_area(?param1) ' +
  'st_asbinary(?param1) st_asewkb(?param1) st_asewkt(?param1) st_asgeojson(?param1) st_asgml(?param1) st_askml(?param1) st_astext(?param1) st_boundary(?param1) st_buffer(?param1,?param2) st_centroid(?param1) ' +
  'st_contains(?param1,?param2) st_convexhull(?param1) st_coorddim(?param1) st_crosses(?param1,?param2) st_curvetoline(?param1) st_difference(?param1,?param2) st_dimension(?param1) st_disjoint(?param1,?param2) ' +
  'st_distance(?param1,?param2) st_dwithin(?param1,?param2,?param3) st_endpoint(?param1) st_envelope(?param1) st_equals(?param1,?param2) st_extent(?param1) st_extent(?param1) st_exteriorring(?param1) ' +
  'st_force_2d(?param1) st_geogfromtext(?param1) st_geogfromwkb(?param1) st_geometryn(?param1,?param2) st_geometrytype(?param1) st_geomfromewkb(?param1) st_geomfromewkt(?param1) st_geomfromgeojson(?param1) ' +
  'st_geomfromgeojson(?param1,?param2) st_geomfromgml(?param1) st_geomfromgml(?param1,?param2) st_geomfromtext(?param1) st_geomfromtext(?param1,?param2) st_geomfromwkb(?param1) st_geomfromwkb(?param1,?param2) ' +
  'st_hasarc(?param1) st_interiorringn(?param1,?param2) st_intersection(?param1,?param2) st_intersects(?param1,?param2) st_isclosed(?param1) st_isempty(?param1) st_isring(?param1) st_issimple(?param1) ' +
  'st_isvalid(?param1) st_length(?param1) st_makeenvelope(?param1,?param2,?param3,?param4) st_makeenvelope(?param1,?param2,?param3,?param4,?param5) st_numgeometries(?param1) st_numinteriorrings(?param1) ' +
  'st_numpoints(?param1) st_orderingequals(?param1,?param2) st_overlaps(?param1,?param2) st_perimeter(?param1) st_point(?param1,?param2) st_pointn(?param1,?param2) st_pointonsurface(?param1) st_polygon(?param1,?param2) ' +
  'st_relate(?param1,?param2) st_relate(?param1,?param2,?param3) st_setsrid(?param1,?param2) st_simplify(?param1,?param2) st_simplifypreservetopology(?param1,?param2) st_snaptogrid(?param1,?param2) st_srid(?param1) ' +
  'st_startpoint(?param1) st_symdifference(?param1,?param2) st_touches(?param1,?param2) st_transform(?param1,?param2) st_union(?param1,?param2) st_within(?param1,?param2) st_x(?param1) st_y(?param1) st_z(?param1) ' +
  'substr(?string,?index) substr(?string,?index,?length) substring(?string,?index) substring(?string,?index,?length) sys_prop(?variablename) tan(?number) teiid_session_get(?param1) teiid_session_set(?param1,?param2) ' +
  'timestampadd(?interval,?count,?timestamp) timestampcreate(?date,?time) timestampdiff(?interval,?timestamp1,?timestamp2) to_bytes(?param1,?param2) to_bytes(?param1,?param2,?param3) to_chars(?param1,?param2) ' +
  'to_chars(?param1,?param2,?param3) to_millis(?param1) tokenize(?param1,?param2) translate(?string,?source,?destination) trim(?spec,?trimChar,?string) ucase(?string) unescape(?string) unix_timestamp(?param1) ' +
  'upper(?string) user() user(?includeSecurityDomain) uuid() week(?date) xmlText(?param1) xmlcomment(?value) xmlconcat(?param1,?param2) xmlpi(?name) xmlpi(?name,?value) xpathvalue(?document,?xpath) ' +
  'xsltransform(?document,?xsl) year(?date)) ';

// Teiid's types from DataTypeManager
// bigdecimal bigint biginteger blob boolean byte char clob date decimal double float geography geometry integer json long null object real short smallint string time timestamp tinyint varbinary varchar xml
// The list below includes the original mysql builtin list plus a number of the Teiid types in the above list
const builtInDatatypes =
  'bool boolean bit blob decimal bigdecimal double float geography geometry json long longblob longtext medium mediumblob mediumint mediumtext smallint string time timestamp tinyblob tinyint tinytext text xml bigint biginteger int int1 int2 int3 int4 int8 integer float4 float8 char varbinary varchar varcharacter precision date datetime year unsigned signed numeric';

const dataTimeWords = 'date time timestamp';

const atomsWords = 'false true null unknown';

const set = (str: string) => {
  const results = {};
  const words = str.split(' ');
  const startZero = 0;
  for (let i = startZero; i < words.length; i++) {
    results[words[i]] = true;
  }
  return results;
};

export const loadDvMime = () => {
  CodeMirror.defineMIME(dvLanguageMode, {
    atoms: set(atomsWords),
    builtin: set(builtInDatatypes),
    dateSQL: set(dataTimeWords),
    keywords: set(keywords + ' ' + functions),
    name: 'sql',
    operatorChars: /^[*+\-%<>!=&|^]/,
  });
};
