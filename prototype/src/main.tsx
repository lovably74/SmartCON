import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

const rootElement = document.getElementById('root')

if (!rootElement) {
  throw new Error('Root element not found')
}

console.log('Starting app render...')

try {
  const root = createRoot(rootElement)
  console.log('Root created, rendering App...')
  root.render(
    <StrictMode>
      <App />
    </StrictMode>
  )
  console.log('App rendered successfully')
} catch (error) {
  console.error('Failed to render app:', error)
  rootElement.innerHTML = `
    <div style="padding: 20px; color: red; font-family: Arial;">
      <h1>애플리케이션 로드 오류</h1>
      <pre style="background: #f0f0f0; padding: 10px; border-radius: 4px;">${String(error)}</pre>
      <p>브라우저 콘솔을 확인하세요 (F12)</p>
      <p>오류 상세: ${error instanceof Error ? error.stack : String(error)}</p>
    </div>
  `
}
