package com.robsonmartins.acaas.t2libras;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.engine.util.Base64;

import com.robsonmartins.acaas.sql.LibrasDAO;
import com.robsonmartins.acaas.t2libras.bean.PalavraBean;
import com.robsonmartins.acaas.t2libras.bean.PalavraDic;
import com.robsonmartins.acaas.t2libras.bean.PalavraSearch;
import com.robsonmartins.acaas.util.CharFilter;

/**
 * Engine de transformacao de Texto para Libras.
 * <br/><br/>
 * Converte Texto em Portugues para Libras atraves de:<br/>
 * <ul>
 * <li>Dicionario de Libras do <a target="_blank" href="http://www.acessobrasil.org.br/libras/">Acessibilidade Brasil</a></li>
 * <li>Dicionario Online de Portugues <a target="_blank" href="http://www.dicio.com.br/">Dicio</a></li>
 * <li>Dicionario Informal <a target="_blank" href="http://www.dicionarioinformal.com.br/">Dicionario inFormal</a></li>
 * </ul>
 * <br/>
 * <b>Idiomas oferecidos</b>:<br/>
 *   <ul><li>Portugues Brasileiro (pt-BR)</li></ul><br/>  
 * <b>Formatos de midia oferecidos</b>:<br/>
 *   <ul>
 *   	<li>Video: OGG (padrao) ou MP4</li>
 *   	<li>Imagem: JPG</li>
 *   </ul><br/>  
 * <b>Modos de retorno JSON</b>:<br/>
 *   <ul>
 *   	<li>0: Compacto - Retorna somente a palavra mais provavel</li>
 *   	<li>1: Padrao - Retorna somente a palavra mais provavel e o UID para o arquivo de midia correspondente</li>
 *   	<li>2: Detalhado - Retorna todas palavras provaveis e todas suas propriedades</li>
 *   </ul><br/>
 *   
 * @author Robson Martins (robson@robsonmartins.com) 
 */
public class AccBrasilTextToLibras implements TextToLibras {

	/** String de Idioma pt-BR (default). */
	private static final String ACCBRASIL_T2LIBRAS_LANG_PT_BR = "pt-BR";
	/** String de formato de arquivo OGG/OGV (default). */
	private static final String ACCBRASIL_T2LIBRAS_FORMAT_OGG_STR = "OGG";
	/** Modo de retorno JSON: modo 0. */
	private static final int ACCBRASIL_T2LIBRAS_MODE_0 = 0;
	/** Modo de retorno JSON: modo 1 (default). */
	private static final int ACCBRASIL_T2LIBRAS_MODE_1 = 1;
	/** Modo de retorno JSON: modo 2. */
	private static final int ACCBRASIL_T2LIBRAS_MODE_2 = 2;
	
	/** Objeto DAO do *ToLibras. */ 
	private static LibrasDAO dao = new LibrasDAO();
	
	/**
	 * Construtor.
	 * @param props Properties com parametros de conexao do BD.<br/>
	 * Parametros de conexao:<br/>
	 * <ul>
	 * 	<li><code>db.driver</code> = Classe do driver JDBC.</li>  
	 * 	<li><code>db.url</code> = URL JDBC de conexao ao BD.</li>  
	 * 	<li><code>db.user</code> = Username do BD.</li>  
	 * 	<li><code>db.password</code> = Password do BD.</li>  
	 * </ul>
	 */
	public AccBrasilTextToLibras(Properties props) {
    	try {
    		String driver = props.getProperty("db.driver"  , "com.mysql.jdbc.Driver");
    		String url    = props.getProperty("db.url"     , "jdbc:mysql://localhost/acaas");
   			String user   = props.getProperty("db.user"    , "acaas");
   			String pwd    = props.getProperty("db.password", "password");
    		Class.forName(driver);
    		dao.open(url, user, pwd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public JSONObject convert(String text, String lang, String format, String mode) throws Exception {
		List<String> expressao = null;
		List<List<PalavraBean>> libras = null;
		if (lang   == null || lang  .equals("")) { lang   = ACCBRASIL_T2LIBRAS_LANG_PT_BR    ; }
		if (format == null || format.equals("")) { format = ACCBRASIL_T2LIBRAS_FORMAT_OGG_STR; }
		int nMode = ACCBRASIL_T2LIBRAS_MODE_1;
		try {
			if (mode == null || mode.equals("")) { throw new NullPointerException(); }
			nMode = Integer.valueOf(mode);
		} catch (Exception e) { }
		expressao = preProcessText(text);
		libras = portToLibras(expressao, (nMode != ACCBRASIL_T2LIBRAS_MODE_2));
		libras = postProcessLibras(libras);
		return librasToJSON(libras, lang, format, nMode);
	}

	@Override
	public JSONObject convert(String text, String mode) throws Exception {
		return convert(text, null, null, mode);
	}

	@Override
	public JSONObject convert(String text) throws Exception {
		return convert(text, null, null, null);
	}
	
	@Override
	public byte[] getMedia(String filename, String lang, String format)	throws Exception {
		/* lang ignorado */
		byte[] data = null;
		if (format == null) { format = ""; }
		if (format.equalsIgnoreCase("mp4") || format.equalsIgnoreCase("video/mp4")) {
			data = getLibrasData("mp4/" + filename);
		} else if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("image/jpeg")) {
			data = getLibrasData("jpg/" + filename);
		} else {
			data = getLibrasData("ogg/" + filename);
		}
		return data;
	}

	@Override
	public byte[] getMedia(String filename) throws Exception {
		return getMedia(filename, null, null);
	}
	
	/**
	 * Realiza o pre-processamento do texto a ser convertido. 
	 * @param text Texto a ser convertido.
	 * @return Lista de tokens para conversao.
	 */
	private List<String> preProcessText(String text) {
		List<String> result = new ArrayList<String>();
		text = text.toUpperCase();
		// pronomes
		text = text.replaceAll("(\\S+)(-ME-)(\\S+)","$1$3 EU")
			.replaceAll("(\\S+)(-TE-)(\\S+)","$1$3 VOC\u00CA")
			.replaceAll("(\\S+)(-LHE-)(\\S+)","$1$3 ELE")
			.replaceAll("(\\S+)(-NOS-)(\\S+)","$1$3 N\u00D3S")
			.replaceAll("(\\S+)(-VOS-)(\\S+)","$1$3 VOC\u00CA DOIS")
			.replaceAll("(\\S+)(-LHES-)(\\S+)","$1$3 VOC\u00CA DOIS");
		text = text.replaceAll("(\\S+)( ME )(\\S+)","$1 $3 EU")
			.replaceAll("(\\S+)( TE )(\\S+)","$1 $3 VOC\u00CA")
			.replaceAll("(\\S+)( LHE )(\\S+)","$1 $3 ELE")
			.replaceAll("(\\S+)( NOS )(\\S+)","$1 $3 N\u00D3S")
			.replaceAll("(\\S+)( VOS )(\\S+)","$1 $3 VOC\u00CA DOIS")
			.replaceAll("(\\S+)( LHES )(\\S+)","$1 $3 VOC\u00CA DOIS");
		// remove caracteres especiais e pontuacao
		text = text.replaceAll("[^\\pL\\pM\\p{Nd}\\p{Nl}\\p{Pc}[\\p{InEnclosedAlphanumerics}&&\\p{So}]]", " ");
		// substitui digitos pela descricao por extenso
		text = text.replaceAll("[1]"," UM "        ).replaceAll("[2]"," DOIS "  )
				   .replaceAll("[3]"," TR\u00CAS " ).replaceAll("[4]"," QUATRO ")
				   .replaceAll("[5]"," CINCO "     ).replaceAll("[6]"," SEIS "  )
				   .replaceAll("[7]"," SETE "      ).replaceAll("[8]"," OITO "  )
				   .replaceAll("[9]"," NOVE "      ).replaceAll("[0]"," ZERO "  );
		text = " " + text + " ";
		// remove e substitui palavras comuns (artigos, preposicoes, etc.)
		text = text.replaceAll("( A )|( O )|( AS )|( OS )|( UMA )|( UMAS )|( UNS )", " ")
			.replaceAll("( NO )|( NA )|( NAS )|( NOS )", " ")
			.replaceAll("( NESSE )|( NESSA )|( NESSES )|( NESSAS )", " ESSE ")
			.replaceAll("( NESTE )|( NESTA )|( NESTES )|( NESTAS )", " ESTE ")
			.replaceAll("( ESSES )|( ESSAS )|( ESSA )", " ESSE ")
			.replaceAll("( ESTES )|( ESTAS )|( ESTA )", " ESTE ")
			.replaceAll("( DE )|( DA )|( DO )|( DAS )|( DOS )|( DUMA )|( DUMAS )|( DUM )|( DUNS )", " ");
		// divide texto em tokens
		String tokens[] = text.split(" ");
		for (int idx = 0; idx < tokens.length; idx++) {
			String palavra = tokens[idx].trim();
			if (palavra.equals("")) { continue; }
			result.add(palavra);
		}
		return result;
	}

	/**
	 * Realiza o pos-processamento do texto ja' convertido para Libras.
	 * @param libras Texto converido em Libras
	 * @return Texto em Libras pos-processado.
	 */
	private List<List<PalavraBean>> postProcessLibras(List<List<PalavraBean>> libras) {
		List<List<PalavraBean>> result = new ArrayList<List<PalavraBean>>();
		List<PalavraBean> token = null, prevToken = null;
		String prevPalavra = null, palavra = null;
		// remove tokens nulos e repetidos
		for (int i = 0; i < libras.size(); i++) {
			token = libras.get(i);
			if (i > 0) { prevToken = libras.get(i - 1); }
			if (token == null || token.size() == 0) { continue; }
			if (prevToken != null && prevToken.size() != 0) { 
				prevPalavra = prevToken.get(0).getPalavra();
			} else {
				prevPalavra = null;
			}
			palavra = token.get(0).getPalavra();
			if (prevPalavra == null || !prevPalavra.equals(palavra)) {
				result.add(token);
			}
		}
		return result;
	}
	
	/**
	 * Gera um UID para um arquivo de midia (video ou imagem). 
	 * @param filename Nome do arquivo de midia.
	 * @param lang Idioma.
	 * @param format Formato de arquivo (JPG, OGG, MP4).
	 * @return UID que aponta para um arquivo de midia.
	 */
	private String generateMediaId(String filename, String lang, String format) {
		Properties prop = new Properties();
		ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
		try {
			if (filename == null || filename.equals("")) { 
				throw new NullPointerException("File not found."); 
			}
			prop.setProperty("filename", filename);
			prop.setProperty("lang", lang);
			prop.setProperty("format", format);
			prop.store(outBuffer, null);
			byte[] data = outBuffer.toByteArray();
			String result = Base64.encode(data, false);
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Converte um Texto em Libras para um objeto JSON. 
	 * @param libras Texto em Libras.
	 * @param lang Idioma.
	 * @param format Formato desejado para o arquivo de midia.
	 * @param mode Modo de retorno JSON: 0 = Compacto, 1 = Padrao, 2 = Detalhado. 
	 * @return Objecto JSON que representa o Texto em Libras.
	 * @throws Exception
	 */
	private JSONObject librasToJSON(List<List<PalavraBean>> libras,
			String lang, String format, int mode) throws Exception {
		
		JSONObject palavraJSON = null;
		JSONArray possibilidades = null;
		JSONObject result = new JSONObject();
		JSONArray palavras = new JSONArray();
		
		for (List<PalavraBean> item : libras) {
			possibilidades = new JSONArray();
			for (PalavraBean palavra : item) {
				String media = generateMediaId(palavra.getMedia(), lang, format);
				palavra.setMedia   (media );
				palavra.setFormat  (format);
				palavra.setLanguage(lang  );

				switch (mode) {
					case ACCBRASIL_T2LIBRAS_MODE_0:
						// modo compacto: retorna somente Palavra mais Provavel.
						palavraJSON = new JSONObject();
						palavraJSON.put("palavra", palavra.getPalavra());
						palavras.put(palavraJSON);
						break;
					case ACCBRASIL_T2LIBRAS_MODE_2:
						// modo detalhado: retorna todas Palavras Possiveis e todas suas propriedades.
						palavraJSON = new JSONObject(palavra);
						possibilidades.put(palavraJSON);
						break;
					case ACCBRASIL_T2LIBRAS_MODE_1:
					default:
						// modo padrao: retorna Palavra mais Provavel e UID da Midia correspondente.
						palavraJSON = new JSONObject();
						palavraJSON.put("palavra", palavra.getPalavra());
						palavraJSON.put("media"  , palavra.getMedia  ());
						palavras.put(palavraJSON);
						break;
				}
			}
			if (mode == ACCBRASIL_T2LIBRAS_MODE_2) {
				JSONObject possJson = new JSONObject();
				possJson.put("possibilidades", possibilidades);
				palavras.put(possJson);
			}
		}
		result.put("items", palavras);
		return result;
	}
	
	/**
	 * Converte uma expressao em portugues para Libras. 
	 * @param expressao Texto a ser convertido (tokens).
	 * @param fast Se true, retorna somente a palavra mais provavel.
	 *   Se false, retorna todas palavras possiveis.
	 * @return Texto em Libras.
	 * @throws Exception
	 */
	private List<List<PalavraBean>> portToLibras(List<String> expressao, boolean fast) throws Exception {
		List<List<PalavraBean>> result = new ArrayList<List<PalavraBean>>();
		List<PalavraBean> possibilidades = new ArrayList<PalavraBean>();
		if (expressao == null) { throw new NullPointerException("Palavra not found."); }
		int first = 0;
		int last = 0;
		PalavraSearch search = new PalavraSearch();
		while (first < expressao.size()) {
			last = expressao.size() - 1;
			// busca em LIBRAS, SINONIMOS, VERBOS, SINONIMOS_VERBOS
			search.setExpressao(expressao);
			search.setFirst(first);	search.setLast(last);
			search = searchAllDict(search, fast, 
					PalavraDic.DIC_LIBRAS, PalavraDic.DIC_SINONIMOS, 
					PalavraDic.DIC_VERBOS, PalavraDic.DIC_SINONIMOS_VERBOS);
			possibilidades = search.getPossibilidades();
			if (possibilidades.size() != 0) {
				result.add(possibilidades);
				first = search.getFirst();
				continue;
			}
			String palavra = expressao.get(first);
			List<List<PalavraBean>> otherSearch = null;
			// nao achou: tenta transformar plural para singular; feminino para masculino
			otherSearch = searchOtherForms(palavra, fast);
			if (otherSearch != null && otherSearch.size() != 0) {
				result.addAll(otherSearch);
				first++;
				continue;
			}
			// nao achou: busca em INFORMAL, INFORMAL_VERBOS
			search.setFirst(first);	search.setLast(last);
			search = searchAllDict(search, fast, 
					PalavraDic.DIC_INFORMAL, PalavraDic.DIC_INFORMAL_VERBOS);
			possibilidades = search.getPossibilidades();
			if (possibilidades.size() != 0) {
				result.add(possibilidades);
				first = search.getFirst();
				continue;
			}
			// nao achou: fazer datililogia
			otherSearch = doDatitlologia(palavra);
			result.addAll(otherSearch);
			first++;
		}
		return processVerbos(result);
	}

	/**
	 * Processa os verbos presentes num Texto em Libras, adicionando pessoa e/ou tempo. 
	 * @param libras Texto em Libras.
	 * @return Texto em Libras processado.
	 * @throws Exception
	 */
	private List<List<PalavraBean>> processVerbos(List<List<PalavraBean>> libras) throws Exception {
		List<List<PalavraBean>> result = new ArrayList<List<PalavraBean>>();
		List<PalavraBean> novoToken = null;
		for (List<PalavraBean> token : libras) {
			for (PalavraBean possibilidade : token) {
				List<PalavraBean> listaPalavras = null;
				// adiciona pessoa se for verbo
				String[] pessoa = {
						possibilidade.getPessoa(), null
				};	
				if (pessoa[0] != null && !pessoa[0].equals("")) {
					// corrige TU = VOCE, ELES = ELE-2 e VOS = VOCE-2
					if (pessoa[0].equals("TU"      )) { pessoa[0] = "VOC\u00CA"; }
					if (pessoa[0].equals("V\u00D3S")) { pessoa[0] = "VOC\u00CA"; pessoa[1] = "DOIS"; }
					if (pessoa[0].equals("ELES"    )) { pessoa[0] = "ELE"      ; pessoa[1] = "DOIS"; }
					listaPalavras = searchOnePalavra(pessoa[0], true, 
								PalavraDic.DIC_LIBRAS, PalavraDic.DIC_SINONIMOS);
					if (listaPalavras.size() != 0) {
						novoToken = new ArrayList<PalavraBean>();
						novoToken.addAll(listaPalavras);
						// adiciona novo token
						result.add(novoToken);
					}
					if (pessoa[1] != null) {
						listaPalavras = searchOnePalavra(pessoa[1], true, 
								PalavraDic.DIC_LIBRAS, PalavraDic.DIC_SINONIMOS);
						if (listaPalavras.size() != 0) {
							novoToken = new ArrayList<PalavraBean>();
							novoToken.addAll(listaPalavras);
							// adiciona novo token
							result.add(novoToken);
						}
					}
				}
				// adiciona token original
				result.add(token);
				// adiciona tempo se for verbo
				String tempo = possibilidade.getTempo();
				if ("FUTURO".equals(tempo) || "PASSADO".equals(tempo)) {
					listaPalavras = searchOnePalavra(tempo, true, 
							PalavraDic.DIC_LIBRAS, PalavraDic.DIC_SINONIMOS);				
					if (listaPalavras.size() != 0) { 
						novoToken = new ArrayList<PalavraBean>();
						novoToken.addAll(listaPalavras);
						// adiciona novo token
						result.add(novoToken);
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Busca outras formas de uma palavra (tentando converter genero, numero, etc). 
	 * @param palavra Palavra a ser buscada.
	 * @param fast Se true, retorna somente a palavra mais provavel.
	 *   Se false, retorna todas palavras possiveis.
	 * @return Texto em Libras.
	 * @throws Exception
	 */
	private List<List<PalavraBean>> searchOtherForms(String palavra, boolean fast) throws Exception {
		List<List<PalavraBean>> result = new ArrayList<List<PalavraBean>>();
		List<PalavraBean> possibilidades = null;
		
		Map<String, Set<String>> palavras = new HashMap<String, Set<String>>();
		palavras.put(palavra, new HashSet<String>());
		
		Set<String> keys = new HashSet<String>(palavras.keySet());
		for (String p: keys) {
			// plural -> singular
			palavras = createOtherForm(palavras, p, "S"  , "V\u00C1RIOS");
			palavras = createOtherForm(palavras, p, "IS" , "V\u00C1RIOS", "", "L");
			palavras = createOtherForm(palavras, p, "ES" , "V\u00C1RIOS", "", "L");
			palavras = createOtherForm(palavras, p, "EIS", "V\u00C1RIOS", "", "L");
			palavras = createOtherForm(palavras, p, "\u00D5ES", "V\u00C1RIOS", "", "\u00C3O");
			palavras = createOtherForm(palavras, p, "\u00C3ES", "V\u00C1RIOS", "", "\u00C3", "\u00C3O");
		}
		keys = new HashSet<String>(palavras.keySet());
		for (String p: keys) {
			// feminino -> masculino
			palavras = createOtherForm(palavras, p, "A", "FEMININO", "", "O", "E");
		}
		keys = new HashSet<String>(palavras.keySet());
		for (String p: keys) {
			// diminutivo
			palavras = createOtherForm(palavras, p, "INHO" , "PEQUENO", "", "O");
			palavras = createOtherForm(palavras, p, "ZINHO", "PEQUENO", "", "O");
			palavras = createOtherForm(palavras, p, "INO"  , "PEQUENO", "", "O");
			palavras = createOtherForm(palavras, p, "ZINO" , "PEQUENO", "", "O");
			// aumentativo
			palavras = createOtherForm(palavras, p, "\u00C3O" , "GRANDE", "", "O");
			palavras = createOtherForm(palavras, p, "Z\u00C3O", "GRANDE", "", "O");
			palavras = createOtherForm(palavras, p, "ONA"     , "GRANDE", "", "O", "A");
			palavras = createOtherForm(palavras, p, "ZONA"    , "GRANDE", "", "O", "A");
		}
		// busca possiveis palavras nos dicionarios
		for (String p: palavras.keySet()) {
			possibilidades = searchOnePalavra(p, fast);
			if (possibilidades.size() != 0) { 
				result.add(possibilidades);
				for (String c: palavras.get(p)) {
					possibilidades = searchOnePalavra(c, fast);
					if (possibilidades.size() != 0) {
						result.add(possibilidades);
					}
				}
				break;
			}
		}
		return result;
	}

	/**
	 * Cria uma outra forma de uma palavra (tentando converter genero, numero, etc). 
	 * @param forms Lista com formas ja' existentes.
	 * @param palavra Palavra original.
	 * @param terminator Terminador original a ser removido.
	 * @param complemento Complemento a ser adicionado (palavra adicional).
	 * @param append Possiveis terminadores a serem agregados 'a palavra original.
	 * @return Lista com formas alternativas de uma palavra.
	 * @throws Exception
	 */
	private Map<String, Set<String>> createOtherForm(Map<String, Set<String>> forms, String palavra,
			String terminator, String complemento, String...append) throws Exception {
		
		if (palavra.endsWith(terminator)) {
			Set<String> complementos = new HashSet<String>(forms.get(palavra));
			complementos.add(complemento);
			if (append == null || append.length == 0) { append = new String[1]; append[0] = ""; }
			for (String a: append) {
				forms.put(palavra.substring(0, palavra.length() - terminator.length()) + a, complementos);
			}
		}
		return forms;
	}
	
	/**
	 * Busca uma unica palavra (ou expressao), recursivamente nos dicionarios. 
	 * @param palavra Palavra (ou expressao) a ser buscada.
	 * @param fast Se true, retorna somente a palavra mais provavel.
	 *   Se false, retorna todas palavras possiveis.
	 * @param dics Lista de dicionarios para buscar. Se null, busca em todos.
	 * @return Lista de possibilidades de uma palavra.
	 * @throws Exception
	 */
	private List<PalavraBean> searchOnePalavra(String palavra, 
			boolean fast, PalavraDic...dics) throws Exception {
		
		List<String> expressao = new ArrayList<String>();
		PalavraSearch search = new PalavraSearch();
		expressao.add(palavra);
		search.setExpressao(expressao);
		search.setFirst(0);	search.setLast(0);
		search = searchAllDict(search, fast, dics);
		return search.getPossibilidades();
	}

	/**
	 * Faz datilologia (soletracao em Libras) de uma palavra. 
	 * @param palavra Palavra a ser soletrada.
	 * @return Texto em Libras.
	 * @throws Exception
	 */
	private List<List<PalavraBean>> doDatitlologia(String palavra) throws Exception {
		List<List<PalavraBean>> result = new ArrayList<List<PalavraBean>>();
		PalavraSearch search = new PalavraSearch();
		Set<String> alternativas = null;
		List<PalavraBean> possibilidades = null;
		List<String> expressao = null;
		String letra = null;
		for (int i = 0; i < palavra.length(); i++) {
			alternativas = new HashSet<String>();
			letra = String.valueOf(palavra.charAt(i));
			alternativas.add(letra);
			alternativas.add(CharFilter.removeAccents(letra));
			for (String alternativa : alternativas) {
				expressao = new ArrayList<String>();
				expressao.add(alternativa);
				search.setFirst(0);	search.setLast(0);
				search.setExpressao(expressao);
				search = searchAllDict(search, true, PalavraDic.DIC_LIBRAS);
				possibilidades = search.getPossibilidades();
				if (possibilidades.size() != 0) { break; }
			}
			if (possibilidades.size() != 0) { result.add(possibilidades); }
		}
		return result;
	}

	/**
	 * Busca nos dicionarios por uma palavra recursivamente, dentro de uma expressao. 
	 * @param search Parametros para busca:<br/>
	 *   <b>expressao</b> = Expressao a ser buscada.<br/> 
	 *   <b>first</b> = Indice da primeira palavra a ser buscada na expressao.<br/> 
	 * @param fast Se true, retorna somente a palavra mais provavel.
	 *   Se false, retorna todas palavras possiveis.
	 * @param dics Lista de dicionarios para buscar. Se null, busca em todos.
	 * @return Resultados da busca:<br/>
	 *   <b>possibilidades</b> = Lista de possiveis palavras.<br/> 
	 *   <b>first</b> = Indice da proxima palavra a ser buscada na expressao.<br/> 
	 *   <b>dic</b> = Dicionario onde a palavra foi encontrada.<br/> 
	 * @throws Exception
	 */
	private PalavraSearch searchAllDict(PalavraSearch search, 
			boolean fast, PalavraDic...dics) throws Exception {

		PalavraDic[] allDics = { 
				PalavraDic.DIC_LIBRAS, 
				PalavraDic.DIC_VERBOS,
				PalavraDic.DIC_SINONIMOS,
				PalavraDic.DIC_SINONIMOS_VERBOS,
				PalavraDic.DIC_INFORMAL,
				PalavraDic.DIC_INFORMAL_VERBOS
		};
		if (dics == null || dics.length == 0) { dics = allDics; }

		List<PalavraBean> possibilidades = new ArrayList<PalavraBean>();
		PalavraSearch result = search;
		int first, last;
		last = result.getExpressao().size() - 1;
		result.setLast(last);
		first = result.getFirst();
		while (last >= first) {
			StringBuilder expressao = new StringBuilder();
			for (int idx = first; idx <= last; idx++) {
				expressao.append(" ").append(result.getExpressao().get(idx));
			}
			for (PalavraDic dic : dics) {
				// busca expressao em cada dict
				List<PalavraBean> libras = 
					getPalavrasByDict(expressao.toString(), dic, fast);
				if (libras != null && libras.size() != 0) {
					first = last + 1;
					// add palavra
					possibilidades.addAll(libras);
					result.setFirst(first);
					result.setPossibilidades(possibilidades);
					result.setDic(dic);
					return result;
				}
			}
			last--;
		}
		result.setPossibilidades(possibilidades);
		return result;
	}
	
	/**
	 * Busca uma palavra (ou expressao) diretamente em um dicionario.
	 * @param expressao Palavra (ou expressao) a ser buscada.
	 * @param dic Dicionario desejado.
	 * @param fast Se true, retorna somente a palavra mais provavel.
	 *   Se false, retorna todas palavras possiveis.
	 * @return Lista de possibilidades de uma palavra (ou expressao).
	 * @throws Exception
	 */
	private List<PalavraBean> getPalavrasByDict(String expressao,
			PalavraDic dic, boolean fast) throws Exception {
		
		List<PalavraBean> possibilidades = new ArrayList<PalavraBean>();
		PalavraBean palavra = null;
		switch (dic) {
			case DIC_LIBRAS:
				palavra = dao.getPalavra(expressao.trim());
				if (palavra != null) { possibilidades.add(palavra);	}
				break;
			case DIC_VERBOS:
				if (fast) {
					palavra = dao.getPalavraByConjugacao(expressao.trim());
					if (palavra != null) { possibilidades.add(palavra); }
				} else {
					possibilidades.addAll(dao.getPalavrasByConjugacao(expressao.trim()));
				}
				break;
			case DIC_SINONIMOS:
				if (fast) {
					palavra = dao.getPalavraBySinonimo(expressao.trim());
					if (palavra != null) { possibilidades.add(palavra);	}
				} else {
					possibilidades.addAll(dao.getPalavrasBySinonimo(expressao.trim()));
				}
				break;
			case DIC_SINONIMOS_VERBOS:
				if (fast) {
					palavra = dao.getPalavraByConjugacaoSinonimo(expressao.trim());
					if (palavra != null) { possibilidades.add(palavra);	}
				} else {
					possibilidades.addAll(dao.getPalavrasByConjugacaoSinonimo(expressao.trim()));
				}
				break;
			case DIC_INFORMAL:
				if (fast) {
					palavra = dao.getPalavraBySinonimoInformal(expressao.trim());
					if (palavra != null) { possibilidades.add(palavra);	}
				} else {
					possibilidades.addAll(dao.getPalavrasBySinonimoInformal(expressao.trim()));
				}
				break;
			case DIC_INFORMAL_VERBOS:
				if (fast) {
					palavra = dao.getPalavraByConjugacaoSinonimoInformal(expressao.trim());
					if (palavra != null) { possibilidades.add(palavra);	}
				} else {
					possibilidades.addAll(dao.getPalavrasByConjugacaoSinonimoInformal(expressao.trim()));
				}
				break;
		}
		return possibilidades;
	}
	
	
	/** 
	 * Retorna dados de uma midia a partir de um nome de arquivo.
	 * @param filename Nome do arquivo de midia (type/id).
	 * @return Dados da midia.
	 * @throws Exception
	 */
	private byte[] getLibrasData(String filename) throws Exception {
		byte[] data = null;
		if (filename == null || filename.equals("")) { 
			throw new NullPointerException("File not found.");
		}
		String[] parts = filename.toUpperCase().split("/");
		if (parts == null || parts.length < 2) {
			throw new NullPointerException("File not found.");
		}
		String type = parts[0];
		String id   = parts[1];
		if (type == null || type.equals("")) {
			throw new NullPointerException("File not found.");
		}
		data = dao.getMedia(id, type);
		if (data == null) {
			throw new NullPointerException("File not found.");
		}
		return data;
	}
}
