// # Package helpers
//
// Validaciones reutilizables de telefono por pais. Este archivo es la fuente
// unica de verdad para reglas de formato de telefono y debe mantenerse
// sincronizado con el mapa DIGIT_COUNTS del frontend (Profile.vue / Registrarse.vue).
package helpers

import (
	"strings"
)

// PrefijoPorPais mapea el nombre del pais (lowercase) al prefijo internacional
// esperado (incluye el signo +). Se usa para extraer los digitos locales con
// precision, ya que prefijos como +1, +52 y +502 tienen longitudes distintas.
//
// Fuente de verdad compartida con el frontend — actualizar ambos al agregar paises.
var PrefijoPorPais = map[string]string{
	"guatemala":             "+502",
	"méxico":                "+52", "mexico": "+52",
	"el salvador":           "+503",
	"honduras":              "+504",
	"nicaragua":             "+505",
	"costa rica":            "+506",
	"panamá":                "+507", "panama": "+507",
	"colombia":              "+57",
	"venezuela":             "+58",
	"ecuador":               "+593",
	"perú":                  "+51", "peru": "+51",
	"bolivia":               "+591",
	"chile":                 "+56",
	"argentina":             "+54",
	"uruguay":               "+598",
	"paraguay":              "+595",
	"brasil":                "+55", "brazil": "+55",
	"cuba":                  "+53",
	"república dominicana":  "+1809", "republica dominicana": "+1809",
	"españa":                "+34", "spain": "+34",
	"united states":         "+1", "estados unidos": "+1",
	"germany":               "+49",
	"france":                "+33",
	"italy":                 "+39",
	"china":                 "+86",
	"japan":                 "+81",
	"india":                 "+91",
	"canada":                "+1",
	"australia":             "+61",
}

// DigitosPorPais mapea el nombre del pais (lowercase) a la cantidad EXACTA
// de digitos locales esperados (sin contar el prefijo internacional).
//
// Fuente de verdad compartida con el frontend — actualizar ambos al agregar paises.
var DigitosPorPais = map[string]int{
	"guatemala":             8,
	"méxico":                10, "mexico": 10,
	"el salvador":           8,
	"honduras":              8,
	"nicaragua":             8,
	"costa rica":            8,
	"panamá":                8, "panama": 8,
	"colombia":              10,
	"venezuela":             10,
	"ecuador":               9,
	"perú":                  9, "peru": 9,
	"bolivia":               8,
	"chile":                 9,
	"argentina":             10,
	"uruguay":               8,
	"paraguay":              9,
	"brasil":                11, "brazil": 11,
	"cuba":                  8,
	"república dominicana":  10, "republica dominicana": 10,
	"españa":                9, "spain": 9,
	"united states":         10, "estados unidos": 10,
	"germany":               10,
	"france":                9,
	"italy":                 10,
	"china":                 11,
	"japan":                 10,
	"india":                 10,
	"canada":                10,
	"australia":             9,
}

// MinDigitosFallback es la cantidad minima de digitos totales aceptada cuando
// el pais del usuario no se encuentra en DigitosPorPais.
const MinDigitosFallback = 7

// ContarDigitos retorna la cantidad de caracteres numericos en un telefono,
// ignorando prefijo, espacios, guiones y parentesis.
//
// Parametros:
//   - telefono: cadena con el numero de telefono
//
// Retorna:
//   - int: cantidad de digitos numericos (0-9) en la cadena
func ContarDigitos(telefono string) int {
	count := 0
	for _, r := range telefono {
		if r >= '0' && r <= '9' {
			count++
		}
	}
	return count
}

// DigitosEsperadosLocal retorna la cantidad exacta de digitos locales esperados
// segun el pais. Si el pais no esta en el mapa, retorna MinDigitosFallback.
//
// Parametros:
//   - pais: nombre del pais tal como viene de Pais.Nombre en la BD (ej: "Guatemala")
//
// Retorna:
//   - int: digitos locales esperados para el pais, o MinDigitosFallback si no se conoce
func DigitosEsperadosLocal(pais string) int {
	if pais == "" {
		return MinDigitosFallback
	}
	key := strings.ToLower(strings.TrimSpace(pais))
	if n, ok := DigitosPorPais[key]; ok {
		return n
	}
	return MinDigitosFallback
}

// ValidarDigitosTelefono verifica que un telefono tenga EXACTAMENTE la cantidad
// de digitos locales que corresponde al pais.
//
// El enfoque usa PrefijoPorPais para saber el prefijo exacto del pais (ej: "+502"
// para Guatemala), lo extrae del inicio del telefono y cuenta solo los digitos
// restantes como "locales". Esto es mas robusto que parsear el prefijo
// dinamicamente, ya que prefijos como +1, +52 y +502 tienen longitudes distintas.
//
// Parametros:
//   - telefono: numero con prefijo internacional, ej: "+502 2345 6789"
//   - pais: nombre del pais tal como viene de Pais.Nombre en la BD (ej: "Guatemala")
//
// Retorna:
//   - ok: true si el numero cumple, false si no
//   - digitosLocales: cantidad de digitos locales detectados (sin el prefijo)
//   - esperados: cantidad esperada segun pais
//
// Notas:
//   - Si pais == "" o no esta en los mapas, aplica solo el minimo fallback
//     sobre el total de digitos del telefono.
//   - La comparacion es exacta (==) cuando el pais se conoce, no solo un minimo.
func ValidarDigitosTelefono(telefono, pais string) (ok bool, digitosLocales, esperados int) {
	key := strings.ToLower(strings.TrimSpace(pais))

	esperados = DigitosEsperadosLocal(pais)

	// Pais desconocido: solo validar minimo de digitos totales
	prefijo, hayPrefijo := PrefijoPorPais[key]
	if pais == "" || !hayPrefijo {
		total := ContarDigitos(telefono)
		return total >= MinDigitosFallback, total, MinDigitosFallback
	}

	// Extraer digitos del prefijo para no contarlos como locales.
	// Normalizamos el telefono y el prefijo quitando todo excepto digitos y el +
	// y buscamos si el telefono comienza con el prefijo (ignorando espacios internos).
	telefonoSoloDigitos := soloDigitosConMas(telefono)
	prefijoSoloDigitos := soloDigitosConMas(prefijo)

	if !strings.HasPrefix(telefonoSoloDigitos, prefijoSoloDigitos) {
		// El telefono no comienza con el prefijo esperado para el pais
		digitosLocales = ContarDigitos(telefono) - ContarDigitos(prefijo)
		if digitosLocales < 0 {
			digitosLocales = 0
		}
		return false, digitosLocales, esperados
	}

	// Digitos locales = total de digitos del telefono menos los del prefijo
	digitosLocales = ContarDigitos(telefono) - ContarDigitos(prefijo)
	return digitosLocales == esperados, digitosLocales, esperados
}

// soloDigitosConMas retorna una cadena conservando solo el '+' inicial y los
// digitos, descartando espacios, guiones y parentesis. Se usa internamente para
// comparar prefijos con el inicio del numero de telefono.
//
// Parametros:
//   - s: cadena con un numero de telefono o prefijo
//
// Retorna:
//   - string: cadena con '+' (si existia al inicio) seguido solo de digitos
func soloDigitosConMas(s string) string {
	var b strings.Builder
	for i, r := range s {
		if i == 0 && r == '+' {
			b.WriteRune(r)
			continue
		}
		if r >= '0' && r <= '9' {
			b.WriteRune(r)
		}
	}
	return b.String()
}
