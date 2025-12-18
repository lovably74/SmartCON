// 임시 테스트 파일 - App이 제대로 렌더링되는지 확인
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'

function TestApp() {
  return (
    <div style={{ padding: '20px', fontFamily: 'Arial' }}>
      <h1 style={{ color: 'red', fontSize: '24px' }}>테스트 화면</h1>
      <p>이 메시지가 보이면 React는 정상 작동 중입니다.</p>
      <div style={{ marginTop: '20px', padding: '10px', background: '#f0f0f0' }}>
        <p>현재 시간: {new Date().toLocaleString()}</p>
      </div>
    </div>
  )
}

const rootElement = document.getElementById('root')
if (rootElement) {
  createRoot(rootElement).render(
    <StrictMode>
      <TestApp />
    </StrictMode>
  )
}

