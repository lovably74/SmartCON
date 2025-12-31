// 간단한 테스트용 App
export default function SimpleApp() {
  return (
    <div style={{ 
      padding: '40px', 
      fontFamily: 'Arial, sans-serif',
      minHeight: '100vh',
      background: '#f5f5f5'
    }}>
      <h1 style={{ color: '#0F172A', fontSize: '32px', marginBottom: '20px' }}>
        SmartCON Lite 테스트
      </h1>
      <div style={{ 
        background: 'white', 
        padding: '20px', 
        borderRadius: '8px',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
      }}>
        <p style={{ fontSize: '18px', color: '#333' }}>
          React가 정상적으로 작동 중입니다!
        </p>
        <p style={{ marginTop: '10px', color: '#666' }}>
          현재 시간: {new Date().toLocaleString('ko-KR')}
        </p>
        <div style={{ marginTop: '20px', padding: '10px', background: '#e3f2fd', borderRadius: '4px' }}>
          <p style={{ margin: 0, color: '#1976d2' }}>
            브라우저 콘솔(F12)을 열어서 JavaScript 오류가 있는지 확인하세요.
          </p>
        </div>
      </div>
    </div>
  )
}



