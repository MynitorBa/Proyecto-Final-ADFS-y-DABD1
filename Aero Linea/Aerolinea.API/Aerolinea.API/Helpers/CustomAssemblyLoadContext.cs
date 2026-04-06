using System.Reflection;
using System.Runtime.Loader;

namespace Aerolinea.API.Helpers
{
    /// <summary>
    /// Contexto de carga de ensamblados personalizado utilizado para cargar librerias nativas
    /// en tiempo de ejecucion de forma aislada, principalmente para la generacion de PDFs.
    /// Hereda de AssemblyLoadContext para permitir la carga de DLLs no administradas por ruta absoluta.
    /// </summary>
    public class CustomAssemblyLoadContext : AssemblyLoadContext
    {
        /// <summary>
        /// Carga una libreria nativa no administrada desde la ruta absoluta indicada
        /// y retorna un handle a la misma.
        /// </summary>
        public IntPtr LoadUnmanagedLibrary(string absolutePath)
        {
            return LoadUnmanagedDll(absolutePath);
        }

        /// <summary>
        /// Resolucion de ensamblados administrados dentro de este contexto.
        /// Retorna null para delegar la carga al contexto predeterminado del runtime.
        /// </summary>
        protected override Assembly? Load(AssemblyName assemblyName)
        {
            return null;
        }
    }
}
