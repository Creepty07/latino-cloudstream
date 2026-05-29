# LatinoPlugins para CloudStream

Repositorio de plugins en **español latino** para CloudStream.

## Plugins incluidos

| Plugin | Fuente | Contenido |
|--------|--------|-----------|
| PelisplusHD | pelisplushd.la | Películas, series, anime latino |
| Latanime | latanime.org | Anime doblado al español latino |

---

## Paso a paso para publicarlo

### Requisitos
- Cuenta en [GitHub](https://github.com)
- [Android Studio](https://developer.android.com/studio) instalado
- [Git](https://git-scm.com/) instalado

---

### 1. Crear el repositorio en GitHub

1. Entra a github.com → botón verde **"New"**
2. Nombre: `latino-cloudstream` (o el que quieras)
3. Marca **Public**
4. Clic en **Create repository**

---

### 2. Subir este código

Abre una terminal en la carpeta `LatinoPlugin/` y ejecuta:

```bash
git init
git add .
git commit -m "Initial plugins"
git branch -M main
git remote add origin https://github.com/TU-USUARIO/latino-cloudstream.git
git push -u origin main
```

---

### 3. Activar GitHub Actions

1. En tu repositorio → pestaña **Settings**
2. Menú izquierdo → **Actions → General**
3. Selecciona **"Allow all actions and reusable workflows"**
4. Baja hasta **"Workflow permissions"** → selecciona **"Read and write permissions"**
5. Clic en **Save**

---

### 4. Ejecutar el build por primera vez

1. Ve a la pestaña **Actions** en GitHub
2. Verás el workflow **"Build Plugins"**
3. Clic en **"Run workflow"** → **"Run workflow"** (botón verde)
4. Espera ~2-3 minutos

Cuando termine, se crea automáticamente la rama **`builds`** con:
- `PelisplusHD.cs3`
- `Latanime.cs3`
- `repo.json`

---

### 5. Obtener la URL de tu repositorio

La URL para pegar en CloudStream será:

```
https://raw.githubusercontent.com/TU-USUARIO/latino-cloudstream/builds/repo.json
```

Reemplaza `TU-USUARIO` con tu nombre de usuario de GitHub.

---

### 6. Instalar en CloudStream

1. Abre CloudStream → ⚙️ **Configuración**
2. **Extensiones** → **Añadir repositorio**
3. Pega la URL del paso 5
4. Busca **PelisplusHD** y **Latanime** → instalar

---

## Agregar más plugins

1. Crea una carpeta nueva: `NuevoPlugin/`
2. Copia la estructura de `PelisplusHD/`
3. Edita el `.kt` con el scraping del nuevo sitio
4. Agrega `include("NuevoPlugin")` en `settings.gradle.kts`
5. Haz push → GitHub Actions recompila todo automáticamente

---

## ⚠️ Nota

Estos plugins son para uso personal. No uses extensiones que alojen
contenido con copyright sin autorización.
